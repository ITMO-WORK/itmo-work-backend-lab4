package org.itmowork.vacancy_service.application.dto.event;

import org.itmowork.vacancy_service.domain.model.VacancyStatusName;

import java.time.Instant;
import java.util.UUID;

public record VacancyStatusChangedEvent(
        UUID vacancyId,
        UUID companyId,
        UUID actorUserId,
        VacancyStatusName oldStatus,
        VacancyStatusName newStatus,
        Instant occurredAt
) {}