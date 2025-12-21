package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.application.port.in.GetAllCompaniesPort;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.domain.model.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllCompaniesUseCase implements GetAllCompaniesPort {

    private final CompanyRepositoryPort companyRepositoryPort;

    @Override
    public Mono<Page<CompanyResponseDto>> getAllCompanies(Pageable pageable) {
        long limit = pageable.getPageSize();
        long offset = pageable.getOffset();

        Mono<Long> totalCount = companyRepositoryPort.count();
        Flux<Company> companies = companyRepositoryPort.findAllCompaniesPaged(limit, offset);

        return totalCount.zipWith(companies.collectList())
                .map(tuple -> {
                    long total = tuple.getT1();
                    List<Company> companyList = tuple.getT2();

                    List<CompanyResponseDto> dtoList = companyList.stream()
                            .map(c ->
                                    CompanyResponseDto.builder()
                                            .id(c.getId())
                                            .name(c.getName())
                                            .email(c.getEmail())
                                            .description(c.getDescription())
                                            .build()
                            ).toList();
                    return new PageImpl<>(dtoList, pageable, total);
                });
    }
}
