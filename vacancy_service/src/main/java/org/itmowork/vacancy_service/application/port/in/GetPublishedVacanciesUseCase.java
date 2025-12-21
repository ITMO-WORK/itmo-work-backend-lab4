package org.itmowork.vacancy_service.application.port.in;

import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.query.GetPublishedVacanciesQuery;
import org.springframework.data.domain.Page;

public interface GetPublishedVacanciesUseCase {
    Page<VacancyResult> getPublished(GetPublishedVacanciesQuery query);
}
