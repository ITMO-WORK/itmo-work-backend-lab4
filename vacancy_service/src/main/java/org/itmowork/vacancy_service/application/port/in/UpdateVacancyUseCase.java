package org.itmowork.vacancy_service.application.port.in;

import org.itmowork.vacancy_service.application.dto.UpdateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;

import java.util.UUID;

public interface UpdateVacancyUseCase {
    VacancyResult update(UUID vacancyId, UpdateVacancyCommand command);
}