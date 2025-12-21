package org.itmowork.vacancy_service.adapter.in.web.mapper;

import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyCreateRequestDto;
import org.itmowork.vacancy_service.adapter.in.web.dto.response.VacancyResponseDto;
import org.itmowork.vacancy_service.application.dto.CreateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.springframework.stereotype.Component;

@Component
public class VacancyCreateWebMapper {

    public CreateVacancyCommand toCommand(VacancyCreateRequestDto request) {
        return new CreateVacancyCommand(
                request.title(),
                request.description(),
                request.salaryFrom(),
                request.salaryTo(),
                request.companyId(),
                request.currencyId()
        );
    }

    public VacancyResponseDto toResponse(VacancyResult result) {
        return VacancyResponseDto.builder()
                .id(result.id())
                .title(result.title())
                .description(result.description())
                .salaryFrom(result.salaryFrom())
                .salaryTo(result.salaryTo())
                .statusId(result.statusId())
                .companyId(result.companyId())
                .currencyId(result.currencyId())
                .build();
    }
}
