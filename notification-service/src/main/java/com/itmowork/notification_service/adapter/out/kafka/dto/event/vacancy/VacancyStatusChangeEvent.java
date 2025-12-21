package com.itmowork.notification_service.adapter.out.kafka.dto.event.vacancy;

import java.util.UUID;

public record VacancyStatusChangeEvent(
        UUID vacancyId,
        UUID companyId,
        UUID actorUserId,
        String oldStatus,
        String newStatus
) {
}
