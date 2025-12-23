package com.itmowork.company_service.adapter.in.web;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.request.CompanyUpdateRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyDeleteResponseDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.application.port.in.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CreateCompanyPort createCompanyPort;
    private final UpdateCompanyPort updateCompanyPort;
    private final DeleteCompanyPort deleteCompanyPort;
    private final GetAllCompaniesPort getAllCompaniesPort;

    @PostMapping("/register-company")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<CompanyResponseDto>> createCompany(@RequestBody @Valid Mono<CompanyRequestDto> companyRequestDto) {
        return companyRequestDto
                .flatMap(createCompanyPort::createCompany)
                .map(companyResponseDto ->
                        ResponseEntity.status(HttpStatus.CREATED).body(companyResponseDto));
    }

    @PatchMapping("/update-company/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<CompanyResponseDto>> updateCompany(@PathVariable UUID id, @RequestBody @Valid Mono<CompanyUpdateRequestDto> companyUpdateRequestDto) {
        return companyUpdateRequestDto
                .flatMap(requestDto -> updateCompanyPort.updateCompany(id, requestDto))
                .map(companyResponseDto ->
                        ResponseEntity.status(HttpStatus.OK).body(companyResponseDto));
    }

    @DeleteMapping("/delete/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<CompanyDeleteResponseDto>> deleteCompany(@PathVariable UUID id) {
        return Mono.from(deleteCompanyPort.deleteCompany(id)
                .map(companyDeleteResponseDto ->
                        ResponseEntity.status(HttpStatus.OK).body(companyDeleteResponseDto)));
    }

    @GetMapping
    public Mono<Page<CompanyResponseDto>> getAllCompanies(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return getAllCompaniesPort.getAllCompanies(pageable);

    }
}