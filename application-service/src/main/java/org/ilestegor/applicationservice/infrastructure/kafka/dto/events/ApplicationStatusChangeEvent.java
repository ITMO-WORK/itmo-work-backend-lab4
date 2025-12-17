package org.ilestegor.applicationservice.infrastructure.kafka.dto.events;

import org.ilestegor.applicationservice.model.ApplicationStatusName;

import java.util.UUID;

public record ApplicationStatusChangeEvent(
        UUID applicationId,
        UUID vacancyId,
        UUID userId,
        String oldStatus,
        String newSta
) {
}
