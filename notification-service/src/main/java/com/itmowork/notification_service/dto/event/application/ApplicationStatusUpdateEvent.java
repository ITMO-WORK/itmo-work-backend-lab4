package com.itmowork.notification_service.dto.event.application;

import java.util.UUID;

public record ApplicationStatusUpdateEvent(
        UUID applicationId,
        UUID vacancyId,
        UUID userId,
        String vacancyTitle,
        String oldStatus,
        String newStatus
) {
}
