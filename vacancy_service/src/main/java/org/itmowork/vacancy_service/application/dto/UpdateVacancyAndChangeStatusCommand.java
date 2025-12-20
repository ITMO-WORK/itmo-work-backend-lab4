package org.itmowork.vacancy_service.application.dto;

import org.itmowork.vacancy_service.domain.model.VacancyStatusName;

import java.util.UUID;

public record UpdateVacancyAndChangeStatusCommand(
        UUID vacancyId,
        UpdateVacancyCommand update,
        VacancyStatusName newStatus
) {}
