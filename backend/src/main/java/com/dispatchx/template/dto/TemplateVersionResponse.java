package com.dispatchx.template.dto;

import com.dispatchx.common.model.ContentType;
import com.dispatchx.common.model.TemplateVersionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TemplateVersionResponse(
        UUID id,
        UUID templateId,
        Integer versionNumber,
        TemplateVersionStatus status,
        String subject,
        String body,
        ContentType contentType,
        List<String> variables,
        Instant createdAt,
        Instant updatedAt
) {}
