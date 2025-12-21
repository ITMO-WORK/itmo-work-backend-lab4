package org.itmowork.vacancy_service.application.dto;

import java.util.UUID;

public record VacancyResult(
        UUID id,
        String title,
        String description,
        Integer salaryFrom,
        Integer salaryTo,
        Long statusId,
        UUID companyId,
        Long currencyId
) {}