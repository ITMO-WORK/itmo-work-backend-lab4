package org.itmo.work.fileservice.adapter.output.kafka.dto.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationCreateEventDto(
        UUID applicationId,
        UUID userId,
        OffsetDateTime createdAt
) {
}
