package org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events;

import java.time.Instant;
import java.util.UUID;

public record ResumeUploadedEventDto(
        UUID fileId,
        UUID userId,
        Instant uploadedAt

) {
}
