package com.dispatchx.notification.dto;

import com.dispatchx.common.model.NotificationStatus;

import java.util.UUID;

/**
 * Response returned after successful cancellation.
 */
public record CancelNotificationResponse(
        UUID id,
        NotificationStatus status
) {}
