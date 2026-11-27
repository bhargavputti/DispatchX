package com.dispatchx.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductionProfileValidator (/ F38 / F02)")
class ProductionProfileValidatorTest {

    private final ProductionProfileValidator validator = new ProductionProfileValidator(new MockEnvironment());

    private MockEnvironment createValidProdEnvironment() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("dispatchx.jwt.secret", "a_very_strong_and_secure_jwt_signing_key_32_chars");
        env.setProperty("dispatchx.admin.default-password", "SecureAdminPass123!");
        env.setProperty("spring.datasource.password", "RealStrongDbSecret456");
        env.setProperty("dispatchx.worker.email.enabled", "false");
        env.setProperty("dispatchx.email.provider", "resend");
        env.setProperty("dispatchx.public-url", "https://dispatchx.example.com");
        env.setProperty("dispatchx.frontend-url", "https://dispatchx.example.com");
        env.setProperty("dispatchx.demo.public", "false");
        env.setProperty("dispatchx.webhook.demo-allowlist", "");
        return env;
    }

    @Test
    @DisplayName("passes when non-prod profile is active even with placeholder secrets")
    void passesOnNonProdProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("local");
        env.setProperty("dispatchx.jwt.secret", ProductionProfileValidator.PLACEHOLDER_JWT_SECRET);
        env.setProperty("dispatchx.admin.default-password", ProductionProfileValidator.PLACEHOLDER_ADMIN_PASSWORD);
        env.setProperty("spring.datasource.password", ProductionProfileValidator.PLACEHOLDER_DB_PASSWORD);
        env.setProperty("dispatchx.demo.public", "true");

        assertThatCode(() -> validator.validate(env))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("passes when prod profile has valid production credentials and configuration")
    void passesOnValidProdConfiguration() {
        MockEnvironment env = createValidProdEnvironment();

        assertThatCode(() -> validator.validate(env))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails fast when JWT secret is placeholder under prod profile")
    void failsOnPlaceholderJwtSecret() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.jwt.secret", ProductionProfileValidator.PLACEHOLDER_JWT_SECRET);

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.jwt.secret");
    }

    @Test
    @DisplayName("fails fast when JWT secret is shorter than 32 chars under prod profile")
    void failsOnShortJwtSecret() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.jwt.secret", "short_secret_12345");

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.jwt.secret");
    }

    @Test
    @DisplayName("fails fast when admin password is placeholder under prod profile")
    void failsOnPlaceholderAdminPassword() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.admin.default-password", ProductionProfileValidator.PLACEHOLDER_ADMIN_PASSWORD);

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.admin.default-password");
    }

    @Test
    @DisplayName("fails fast when admin password is shorter than 10 chars under prod profile")
    void failsOnShortAdminPassword() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.admin.default-password", "Short123!");

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.admin.default-password");
    }

    @Test
    @DisplayName("fails fast when datasource password is placeholder or blank under prod profile")
    void failsOnPlaceholderDbPassword() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("spring.datasource.password", ProductionProfileValidator.PLACEHOLDER_DB_PASSWORD);

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.password");
    }

    @Test
    @DisplayName("fails fast when email worker is enabled with fake provider under prod profile")
    void failsOnFakeEmailProviderWhenWorkerEnabled() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.worker.email.enabled", "true");
        env.setProperty("dispatchx.email.provider", "fake");

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.email.provider");
    }

    @Test
    @DisplayName("fails fast when demo mode is public under prod profile")
    void failsOnPublicDemoModeInProd() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.demo.public", "true");

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.demo.public");
    }

    @Test
    @DisplayName("fails fast when webhook demo allowlist is populated under prod profile")
    void failsOnWebhookDemoAllowlistInProd() {
        MockEnvironment env = createValidProdEnvironment();
        env.setProperty("dispatchx.webhook.demo-allowlist", "localhost,127.0.0.1");

        assertThatThrownBy(() -> validator.validate(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dispatchx.webhook.demo-allowlist");
    }

    @Test
    @DisplayName("declares Ordered.HIGHEST_PRECEDENCE so security checks execute before any data bootstrap runner")
    void declaresHighestPrecedenceOrder() {
        Order orderAnnotation = ProductionProfileValidator.class.getAnnotation(Order.class);
        assertThat(orderAnnotation).isNotNull();
        assertThat(orderAnnotation.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }
}

