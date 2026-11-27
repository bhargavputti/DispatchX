package com.dispatchx.notification.dto;

import com.dispatchx.common.model.Actor;
import com.dispatchx.common.model.NotificationStatus;

import java.time.Instant;

/**
 * DTO representing an entry in the immutable notification status history audit trail.
 */
public record NotificationHistoryResponse(
        Long id,
        NotificationStatus fromStatus,
        NotificationStatus toStatus,
        String reason,
        Actor actor,
        Instant occurredAt
) {}
