package org.itmowork.vacancy_service.application.usecase;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.application.port.in.VacancyQueriesUseCase;
import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;

import java.util.UUID;

@RequiredArgsConstructor
public class VacancyQueriesService implements VacancyQueriesUseCase {

    private final VacancyRepositoryPort vacancyRepositoryPort;

    @Override
    public UUID getCompanyId(UUID vacancyId) {
        UUID companyId = vacancyRepositoryPort.findCompanyId(vacancyId);
        if (companyId == null) {
            throw new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found");
        }
        return companyId;
    }

    @Override
    public String getTitle(UUID vacancyId) {
        String title = vacancyRepositoryPort.findTitle(vacancyId);
        if (title == null) {
            throw new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found");
        }
        return title;
    }

    @Override
    public boolean isPublished(UUID vacancyId) {
        Boolean published = vacancyRepositoryPort.isPublished(vacancyId);
        if (published == null) {
            throw new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found");
        }
        return published;
    }

    @Override
    public boolean exists(UUID vacancyId) {
        return vacancyRepositoryPort.existsById(vacancyId);
    }
}