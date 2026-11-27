package com.dispatchx.delivery.provider;

import com.dispatchx.delivery.provider.fake.FakeEmailProperties;
import com.dispatchx.delivery.provider.fake.FakeEmailProvider;
import com.dispatchx.delivery.provider.guard.ProductionProviderGuard;
import com.dispatchx.delivery.provider.resend.ResendEmailProvider;
import com.dispatchx.delivery.provider.resend.ResendProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** R8: production guard, conditional provider beans, Resend requires a key. */
class ProductionProviderGuardTest {

    @Configuration
    @EnableConfigurationProperties({FakeEmailProperties.class, ResendProperties.class})
    @Import({FakeEmailProvider.class, ResendEmailProvider.class})
    static class ProvidersConfig {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(ProvidersConfig.class);

    private static MockEnvironment env(String provider, String... profiles) {
        MockEnvironment environment = new MockEnvironment();
        if (provider != null) {
            environment.setProperty("dispatchx.email.provider", provider);
        }
        environment.setActiveProfiles(profiles);
        return environment;
    }

    @Test
    @DisplayName("R8 the fake provider is rejected under the prod profile, explicitly or by default")
    void prodRejectsFake() {
        assertThatThrownBy(() -> new ProductionProviderGuard(env("fake", "prod")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prod");
        assertThatThrownBy(() -> new ProductionProviderGuard(env(null, "prod")))
                .as("unset provider defaults to fake and is rejected").isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new ProductionProviderGuard(env("fake", "local", "prod")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("R8 prod with the resend provider, and non-prod profiles with either provider, start")
    void allowedCombinations() {
        assertThatCode(() -> new ProductionProviderGuard(env("resend", "prod"))).doesNotThrowAnyException();
        assertThatCode(() -> new ProductionProviderGuard(env(null))).doesNotThrowAnyException();
        assertThatCode(() -> new ProductionProviderGuard(env("fake", "local"))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("R8 by default only the fake provider bean exists")
    void defaultIsFake() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(FakeEmailProvider.class);
            assertThat(context).doesNotHaveBean(ResendEmailProvider.class);
            assertThat(context).hasSingleBean(EmailProvider.class);
        });
    }

    @Test
    @DisplayName("R8 dispatchx.email.provider=fake selects only the fake provider")
    void fakeSelected() {
        runner.withPropertyValues("dispatchx.email.provider=fake").run(context -> {
            assertThat(context).hasSingleBean(FakeEmailProvider.class);
            assertThat(context).doesNotHaveBean(ResendEmailProvider.class);
        });
    }

    @Test
    @DisplayName("R8 dispatchx.email.provider=resend selects only the Resend provider when a key is configured")
    void resendSelected() {
        runner.withPropertyValues("dispatchx.email.provider=resend", "dispatchx.resend.api-key=test-key-not-real").run(context -> {
            assertThat(context).hasSingleBean(ResendEmailProvider.class);
            assertThat(context).doesNotHaveBean(FakeEmailProvider.class);
            assertThat(context.getBean(EmailProvider.class).name()).isEqualTo("resend");
        });
    }
    @Test
    @DisplayName("R8 the Resend provider fails startup without an API key and never prints the key")
    void resendRequiresKey() {
        // The OS environment is removed for the unconfigured case on purpose. A developer machine that has ever
        // done a live Resend send carries DISPATCHX_RESEND_API_KEY as a user-scope variable, and Spring's relaxed
        // binding turns that into dispatchx.resend.api-key - so "no key is configured anywhere" is simply not true in
        // such a shell, and this assertion passed or failed depending on who ran it. Dropping the system-environment
        // property source expresses the actual intent (nothing supplies a key) without weakening anything: both
        // assertions below are unchanged, and the blank-key case after it still runs against the full environment.
        runner.withInitializer(ProductionProviderGuardTest::withoutTheOsEnvironment)
                .withPropertyValues("dispatchx.email.provider=resend").run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("dispatchx.resend.api-key");
                });
        runner.withPropertyValues("dispatchx.email.provider=resend", "dispatchx.resend.api-key=   ").run(context ->
                assertThat(context).hasFailed());
        ResendProperties properties = new ResendProperties("super-secret-key", "https://api.resend.com",
                java.time.Duration.ofSeconds(2), java.time.Duration.ofSeconds(8), 65536);
        assertThat(properties.toString()).doesNotContain("super-secret-key").contains("<redacted>");
    }

    /**
     * Removes the OS environment property source from a test context, so an ambient {@code DISPATCHX_*} variable on
     * the machine running the suite cannot stand in for configuration the test is asserting is absent.
     */
    private static void withoutTheOsEnvironment(org.springframework.context.ConfigurableApplicationContext context) {
        context.getEnvironment().getPropertySources()
                .remove(org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
    }
}
