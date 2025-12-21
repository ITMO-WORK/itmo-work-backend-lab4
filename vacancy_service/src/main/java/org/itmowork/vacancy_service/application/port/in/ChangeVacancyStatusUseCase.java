package org.itmowork.vacancy_service.application.port.in;

import org.itmowork.vacancy_service.application.dto.ChangeVacancyStatusCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;

public interface ChangeVacancyStatusUseCase {
    VacancyResult changeStatus(ChangeVacancyStatusCommand command);
}