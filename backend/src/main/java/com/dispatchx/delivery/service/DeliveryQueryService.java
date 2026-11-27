package com.dispatchx.delivery.service;

import com.dispatchx.common.model.AttemptOutcome;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read-only boundary of the delivery feature for admin reads (/ , seam M2).
 *
 * <p>The admin feature depends on this interface, never on {@code DeliveryAttemptRepository} or the entity.
 */
public interface DeliveryQueryService {

    List<AttemptView> attemptsForNotification(UUID tenantId, UUID notificationId);

    record AttemptView(
            int attemptNumber,
            int deliveryCycle,
            String provider,
            Instant startedAt,
            Instant finishedAt,
            AttemptOutcome outcome,
            String errorCode,
            Integer httpStatus,
            long latencyMs
    ) {}
}
