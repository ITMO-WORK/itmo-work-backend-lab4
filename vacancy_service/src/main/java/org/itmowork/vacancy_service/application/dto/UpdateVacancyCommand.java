package org.itmowork.vacancy_service.application.dto;

public record UpdateVacancyCommand(
        String title,
        String description,
        Integer salaryFrom,
        Integer salaryTo,
        Long currencyId
) {}