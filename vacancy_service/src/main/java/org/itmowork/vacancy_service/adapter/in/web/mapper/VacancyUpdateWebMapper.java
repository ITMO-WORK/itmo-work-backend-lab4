package org.itmowork.vacancy_service.adapter.in.web.mapper;

import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyCommand;
import org.springframework.stereotype.Component;

@Component
public class VacancyUpdateWebMapper {

    public UpdateVacancyCommand toCommand(VacancyUpdateRequestDto dto) {
        return new UpdateVacancyCommand(
                dto.title(),
                dto.description(),
                dto.salaryFrom(),
                dto.salaryTo(),
                dto.currencyId()
        );
    }
}