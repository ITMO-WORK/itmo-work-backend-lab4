package org.itmowork.vacancy_service.application.dto.query;

import org.springframework.data.domain.Pageable;

public record GetPublishedVacanciesQuery(Pageable pageable) {}