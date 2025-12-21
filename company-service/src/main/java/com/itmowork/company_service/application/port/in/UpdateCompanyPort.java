package com.itmowork.company_service.application.port.in;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyUpdateRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateCompanyPort {

    Mono<CompanyResponseDto> updateCompany(UUID id, CompanyUpdateRequestDto companyUpdateRequestDto);
}
