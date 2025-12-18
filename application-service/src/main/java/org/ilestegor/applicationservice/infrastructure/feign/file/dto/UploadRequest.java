package org.ilestegor.applicationservice.infrastructure.feign.file.dto;

import java.util.UUID;

public record UploadRequest(
        UUID applicationId,
        UUID userId
) {
}
