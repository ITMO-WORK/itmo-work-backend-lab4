package org.ilestegor.applicationservice.dirty.infrastructure.feign.file.dto;

import java.util.UUID;

public record UploadRequest(
        UUID applicationId,
        UUID userId
) {
}
