package org.itmowork.vacancy_service.application.dto;

import java.util.UUID;

public record CreateVacancyCommand(
        String title,
        String description,
        Integer salaryFrom,
        Integer salaryTo,
        UUID companyId,
        Long currencyId
) {}