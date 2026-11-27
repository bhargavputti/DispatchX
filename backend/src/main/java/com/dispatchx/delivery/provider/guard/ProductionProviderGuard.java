package com.dispatchx.delivery.provider.guard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Startup guard: the deterministic fake email provider can never run under the {@code prod} profile
 * (CONTRACTS.md Provider contracts). Fails the application context fast instead of silently installing a stub as
 * functional release behavior.
 */
@Component
public class ProductionProviderGuard {

    @Autowired
    public ProductionProviderGuard(Environment environment) {
        String provider = environment.getProperty("dispatchx.email.provider", "fake").trim();
        if (environment.acceptsProfiles(Profiles.of("prod")) && !"resend".equalsIgnoreCase(provider)) {
            throw new IllegalStateException(
                    "dispatchx.email.provider='" + provider + "' is not allowed with the 'prod' profile; "
                            + "configure dispatchx.email.provider=resend with DISPATCHX_RESEND_API_KEY");
        }
    }
}
