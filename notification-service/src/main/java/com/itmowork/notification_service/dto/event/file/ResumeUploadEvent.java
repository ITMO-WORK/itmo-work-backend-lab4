package com.itmowork.notification_service.dto.event.file;

import java.time.Instant;
import java.util.UUID;

public record ResumeUploadEvent(
        UUID fileId,
        UUID userId,
        String originalFileName,
        String contentType,
        Instant uploadedAt
) {
}
