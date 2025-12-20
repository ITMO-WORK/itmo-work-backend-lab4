package org.itmowork.vacancy_service.application.port.in;

import org.itmowork.vacancy_service.application.dto.UpdateVacancyAndChangeStatusCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;

public interface UpdateVacancyAndChangeStatusUseCase {
    VacancyResult updateAndChangeStatus(UpdateVacancyAndChangeStatusCommand command);
}