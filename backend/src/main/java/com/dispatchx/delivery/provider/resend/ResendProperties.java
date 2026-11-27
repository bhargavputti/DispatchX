package com.dispatchx.delivery.provider.resend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Resend adapter settings ({@code dispatchx.resend.*}). The API key is never printed by {@link #toString()}.
 */
@ConfigurationProperties(prefix = "dispatchx.resend")
public record ResendProperties(
        @DefaultValue("") String apiKey,
        @DefaultValue("https://api.resend.com") String baseUrl,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("8s") Duration readTimeout,
        @DefaultValue("65536") int maxResponseBytes
) {
    @Override
    public String toString() {
        return "ResendProperties[baseUrl=" + baseUrl + ", connectTimeout=" + connectTimeout
                + ", readTimeout=" + readTimeout + ", maxResponseBytes=" + maxResponseBytes + ", apiKey=<redacted>]";
    }
}
