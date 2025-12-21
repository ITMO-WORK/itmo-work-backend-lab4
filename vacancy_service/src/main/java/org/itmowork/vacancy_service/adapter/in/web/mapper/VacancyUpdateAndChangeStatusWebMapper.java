package org.itmowork.vacancy_service.adapter.in.web.mapper;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyAndChangeStatusCommand;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyCommand;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VacancyUpdateAndChangeStatusWebMapper {

    public UpdateVacancyAndChangeStatusCommand toCommand(
            UUID vacancyId,
            VacancyUpdateRequestDto dto,
            VacancyStatusName newStatus
    ) {
        UpdateVacancyCommand update = new UpdateVacancyCommand(
                dto.title(),
                dto.description(),
                dto.salaryFrom(),
                dto.salaryTo(),
                dto.currencyId()
        );

        return new UpdateVacancyAndChangeStatusCommand(vacancyId, update, newStatus);
    }
}