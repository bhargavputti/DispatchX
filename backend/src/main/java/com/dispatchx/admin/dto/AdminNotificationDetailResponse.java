package com.dispatchx.admin.dto;

import com.dispatchx.common.model.Channel;
import com.dispatchx.common.model.NotificationStatus;
import com.dispatchx.delivery.service.DeliveryQueryService.AttemptView;
import com.dispatchx.notification.service.NotificationQueryService.StatusTransition;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detailed admin notification view with the delivery-attempt timeline and status-transition audit ().
 * Rendered body is never included.
 */
public record AdminNotificationDetailResponse(
        UUID id,
        UUID tenantId,
        Channel channel,
        NotificationStatus status,
        String recipient,
        String subject,
        Instant scheduledAt,
        Instant createdAt,
        Instant firstProcessingAt,
        Instant completedAt,
        int attemptCount,
        String lastErrorCode,
        String provider,
        String providerMessageId,
        String metadata,
        List<AttemptView> deliveryAttempts,
        List<StatusTransition> statusHistory
) {}
