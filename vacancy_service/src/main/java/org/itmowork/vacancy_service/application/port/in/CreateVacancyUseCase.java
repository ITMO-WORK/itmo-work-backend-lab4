package org.itmowork.vacancy_service.application.port.in;

import org.itmowork.vacancy_service.application.dto.CreateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;

public interface CreateVacancyUseCase {
    VacancyResult create(CreateVacancyCommand command, VacancyStatusName initialStatus);
}