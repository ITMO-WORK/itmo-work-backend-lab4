package org.itmowork.vacancy_service.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.out.persistence.repository.VacancyStatusRepository;
import org.itmowork.vacancy_service.application.port.out.VacancyStatusRepositoryPort;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyStatusNotFoundException;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacancyStatusJpaAdapter implements VacancyStatusRepositoryPort {

    private final VacancyStatusRepository vacancyStatusRepository;

    @Override
    public VacancyStatus findByNameOrThrow(VacancyStatusName statusName) {
        return vacancyStatusRepository.findByVacancyStatusName(statusName)
                .orElseThrow(VacancyStatusNotFoundException::new);
    }
}