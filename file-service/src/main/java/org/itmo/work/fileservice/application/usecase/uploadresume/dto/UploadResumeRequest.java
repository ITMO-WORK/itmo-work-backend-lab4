package org.itmo.work.fileservice.application.usecase.uploadresume.dto;

import java.util.UUID;

public record UploadResumeRequest(
        UUID applicationId,
        UUID userId
) {
}
