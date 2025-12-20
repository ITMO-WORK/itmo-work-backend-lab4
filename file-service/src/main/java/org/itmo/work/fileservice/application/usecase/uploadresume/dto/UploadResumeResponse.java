package org.itmo.work.fileservice.application.usecase.uploadresume.dto;

import java.util.UUID;

public record UploadResumeResponse(
        UUID fileId,
        UUID objectKey
) {
}
