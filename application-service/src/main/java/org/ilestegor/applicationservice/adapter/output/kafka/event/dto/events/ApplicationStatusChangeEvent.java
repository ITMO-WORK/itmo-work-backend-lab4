package org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events;

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
