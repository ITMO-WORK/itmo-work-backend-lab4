package org.itmowork.vacancy_service.dto.kafka;

import java.util.UUID;

public record VacancyStatusChangePayload(
        UUID vacancyId,
        UUID companyId,
        UUID actorUserId,
        String oldStatus,
        String newStatus
) { }