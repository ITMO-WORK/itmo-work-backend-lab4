package org.itmowork.vacancy_service.application.usecase;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.query.GetPublishedVacanciesQuery;
import org.itmowork.vacancy_service.application.port.in.GetPublishedVacanciesUseCase;
import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class GetPublishedVacanciesService implements GetPublishedVacanciesUseCase {

    private final VacancyRepositoryPort vacancyRepositoryPort;

    @Override
    public Page<VacancyResult> getPublished(GetPublishedVacanciesQuery query) {
        return vacancyRepositoryPort.findPublished(query.pageable())
                .map(v -> new VacancyResult(
                        v.getId(),
                        v.getTitle(),
                        v.getDescription(),
                        v.getSalaryFrom(),
                        v.getSalaryTo(),
                        v.getStatus().getId(),
                        v.getCompanyId(),
                        v.getCurrency().getId()
                ));
    }
}