package org.itmowork.vacancy_service.application.port.out;

import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface VacancyRepositoryPort {
    Vacancy save(Vacancy vacancy);
    Optional<Vacancy> findById(UUID vacancyId);
    Page<Vacancy> findPublished(Pageable pageable);
    UUID findCompanyId(UUID vacancyId);
    String findTitle(UUID vacancyId);
    Boolean isPublished(UUID vacancyId);
    boolean existsById(UUID vacancyId);
}