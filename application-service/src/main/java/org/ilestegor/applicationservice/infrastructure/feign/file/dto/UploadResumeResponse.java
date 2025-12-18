package org.ilestegor.applicationservice.infrastructure.feign.file.dto;

import java.util.UUID;

public record UploadResumeResponse(
        UUID fieldId,
        UUID replacedField
) {
}
