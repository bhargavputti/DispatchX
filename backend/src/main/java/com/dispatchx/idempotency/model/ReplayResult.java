package com.dispatchx.idempotency.model;

import com.dispatchx.common.model.NotificationStatus;

import java.util.UUID;

/**
 * Result representing a verified idempotent replay.
 */
public record ReplayResult(
        UUID notificationId,
        NotificationStatus acceptanceStatus
) {}
