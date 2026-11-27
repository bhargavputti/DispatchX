package com.dispatchx.template.dto;

import com.dispatchx.common.model.ContentType;

public record RenderedTemplateResponse(
        String subject,
        String body,
        ContentType contentType
) {}
