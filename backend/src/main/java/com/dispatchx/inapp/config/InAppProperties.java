package com.dispatchx.inapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * In-app read and drain limits ({@code dispatchx.inapp.*}, / ).
 *
 * <p>{@code catchUpLimit} is the accepted 100 of CONTRACTS §SSE: a drain reads one more row than this and emits
 * {@code resync} when that extra row exists, so the decision is made on <b>actual eligible rows</b> and never on
 * arithmetic over sequence values ().
 */
@ConfigurationProperties(prefix = "dispatchx.inapp")
public record InAppProperties(
        @DefaultValue("100") int catchUpLimit,
        @DefaultValue("50") int pageSize,
        @DefaultValue("200") int maxPageSize
) {

    public InAppProperties {
        if (catchUpLimit < 1) {
            throw new IllegalArgumentException("dispatchx.inapp.catch-up-limit must be at least 1");
        }
        if (pageSize < 1 || maxPageSize < pageSize) {
            throw new IllegalArgumentException("dispatchx.inapp.page-size must be positive and at most max-page-size");
        }
    }
}