package org.itmowork.vacancy_service.application.port.out;

import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;

public interface VacancyStatusRepositoryPort {
    VacancyStatus findByNameOrThrow(VacancyStatusName statusName);
}
