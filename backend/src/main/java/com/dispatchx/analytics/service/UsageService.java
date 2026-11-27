package com.dispatchx.analytics.service;

import com.dispatchx.analytics.dto.UsageSummaryResponse;
import com.dispatchx.analytics.dto.UsageTimeseriesResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Tenant business-usage analytics boundary (/ ). Consumed by the tenant {@code UsageController} and,
 * for per-tenant admin breakdowns, by the admin analytics service.
 */
public interface UsageService {

    UsageSummaryResponse summary(UUID tenantId, Instant from, Instant to);

    UsageTimeseriesResponse timeseries(UUID tenantId, Instant from, Instant to, String interval);
}
