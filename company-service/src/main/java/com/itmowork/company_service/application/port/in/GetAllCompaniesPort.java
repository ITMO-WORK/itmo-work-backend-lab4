package com.itmowork.company_service.application.port.in;

import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface GetAllCompaniesPort {

    Mono<Page<CompanyResponseDto>> getAllCompanies(Pageable pageable);
}
