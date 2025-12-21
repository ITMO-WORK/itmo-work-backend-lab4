package org.itmowork.vacancy_service.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.out.persistence.repository.VacancyRepository;
import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VacancyJpaAdapter implements VacancyRepositoryPort {

    private final VacancyRepository vacancyRepository;

    @Override
    public Vacancy save(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }

    @Override
    public Optional<Vacancy> findById(UUID vacancyId) {
        return vacancyRepository.findById(vacancyId);
    }

    @Override
    public Page<Vacancy> findPublished(Pageable pageable) {
        return vacancyRepository.getAllPublished(pageable);
    }

    @Override
    public UUID findCompanyId(UUID vacancyId) {
        return vacancyRepository.findCompanyId(vacancyId);
    }

    @Override
    public String findTitle(UUID vacancyId) {
        return vacancyRepository.findTitleById(vacancyId);
    }

    @Override
    public Boolean isPublished(UUID vacancyId) {
        return vacancyRepository.isPublished(vacancyId);
    }

    @Override
    public boolean existsById(UUID vacancyId) {
        return vacancyRepository.existsVacanciesById(vacancyId);
    }
}