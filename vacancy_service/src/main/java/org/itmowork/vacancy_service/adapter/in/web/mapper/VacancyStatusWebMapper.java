package org.itmowork.vacancy_service.adapter.in.web.mapper;

import org.itmowork.vacancy_service.application.dto.ChangeVacancyStatusCommand;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VacancyStatusWebMapper {

    public ChangeVacancyStatusCommand toCommand(UUID vacancyId, VacancyStatusName newStatus) {
        return new ChangeVacancyStatusCommand(vacancyId, newStatus);
    }
}