package org.itmo.work.fileservice.infrastructure.dto.events;

import java.time.Instant;
import java.util.UUID;

public record FileUploadEvent(
        UUID fileId,
        UUID userId,
        String originalFileName,
        String contentType,
        Instant uploadedAt

) {
}
