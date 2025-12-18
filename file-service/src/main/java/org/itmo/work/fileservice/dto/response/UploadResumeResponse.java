package org.itmo.work.fileservice.dto.response;

import java.util.UUID;

public record UploadResumeResponse(
        UUID fieldId,
        UUID replacedField
) {
}
