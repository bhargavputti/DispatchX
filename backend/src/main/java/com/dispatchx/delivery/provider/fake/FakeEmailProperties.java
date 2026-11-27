package com.dispatchx.delivery.provider.fake;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Knobs of the deterministic fake email provider ({@code dispatchx.fake-email.*}).
 * Rates are probabilities in [0, 1]; a non-null {@code seed} makes the random outcomes reproducible.
 */
@ConfigurationProperties(prefix = "dispatchx.fake-email")
public record FakeEmailProperties(
        @DefaultValue("0") long latencyMs,
        @DefaultValue("0") double tempFailureRate,
        @DefaultValue("0") double permFailureRate,
        Long seed,
        @DefaultValue("5") int deferSeconds
) {
    public FakeEmailProperties {
        if (latencyMs < 0 || latencyMs > 60_000) {
            throw new IllegalArgumentException("dispatchx.fake-email.latency-ms must be between 0 and 60000");
        }
        if (tempFailureRate < 0 || tempFailureRate > 1 || permFailureRate < 0 || permFailureRate > 1) {
            throw new IllegalArgumentException("dispatchx.fake-email failure rates must be between 0 and 1");
        }
        if (deferSeconds < 1) {
            throw new IllegalArgumentException("dispatchx.fake-email.defer-seconds must be at least 1");
        }
    }

    public static FakeEmailProperties defaults() {
        return new FakeEmailProperties(0, 0, 0, null, 5);
    }
}
