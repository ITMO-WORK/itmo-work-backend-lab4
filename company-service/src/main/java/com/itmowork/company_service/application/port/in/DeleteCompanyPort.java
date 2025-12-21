package com.itmowork.company_service.application.port.in;

import com.itmowork.company_service.adapter.in.web.dto.response.CompanyDeleteResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteCompanyPort {

    Mono<CompanyDeleteResponseDto> deleteCompany(UUID id);
}
