package org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationCreateEventDto(
        UUID applicationId,
        UUID userId,
        OffsetDateTime createdAt
) {
}
