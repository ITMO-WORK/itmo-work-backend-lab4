package org.itmowork.vacancy_service.adapter.out.kafka.dto;

import java.util.UUID;

public record VacancyStatusChangePayload(
        UUID vacancyId,
        UUID companyId,
        UUID actorUserId,
        String oldStatus,
        String newStatus
) {}