package com.dispatchx.template.dto;

import com.dispatchx.common.model.ContentType;

import java.util.UUID;

/**
 * Resolved and rendered snapshot of a template version for a notification.
 */
public record ResolvedTemplateSnapshot(
        UUID templateVersionId,
        String renderedSubject,
        String renderedBody,
        ContentType contentType
) {}
