package com.itmowork.company_service.application.port.in;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import reactor.core.publisher.Mono;

public interface CreateCompanyPort {

    Mono<CompanyResponseDto> createCompany(CompanyRequestDto companyRequestDto);
}
