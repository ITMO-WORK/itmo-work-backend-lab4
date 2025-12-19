package org.ilestegor.applicationservice.dirty.infrastructure.kafka.dto.events;

import java.util.UUID;

public record ApplicationStatusChangeEvent(
        UUID applicationId,
        UUID vacancyId,
        UUID userId,
        String vacancyTitle,
        String oldStatus,
        String newStatus
) {
}
