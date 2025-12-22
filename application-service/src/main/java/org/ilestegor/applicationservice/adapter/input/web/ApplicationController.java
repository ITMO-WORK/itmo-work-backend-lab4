package org.ilestegor.applicationservice.adapter.input.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationCreateResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationStatusUpdateResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.GetMyApplicationResponse;
import org.ilestegor.applicationservice.application.port.input.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final CreateApplicationPort createApplicationPort;
    private final UpdateApplicationPort updateApplicationPort;
    private final UpdateApplicationStatusPort updateApplicationStatusPort;
    private final GetAllApplicationsByVacancyIdPort getAllApplicationsByVacancyIdPort;
    private final GetApplicationPort getApplicationPort;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<ApplicationCreateResponseDto>> createApplication(@RequestParam UUID vacancyId, @RequestBody @Valid ApplicationCreateRequestDto dto) {
        return createApplicationPort.createApplication(vacancyId, dto).map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PatchMapping("/{applicationId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<ApplicationCreateResponseDto>> updateApplication(@PathVariable UUID applicationId, @RequestBody ApplicationCreateRequestDto applicationCreateRequestDto) {
        return updateApplicationPort.updateApplication(applicationId, applicationCreateRequestDto).map(body -> new ResponseEntity<>(body, HttpStatus.OK));
    }

    @PatchMapping("/{applicationId}/status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER', 'EMPLOYEE')")
    public Mono<ResponseEntity<ApplicationStatusUpdateResponseDto>> updateApplicationStatus(@PathVariable UUID applicationId, @RequestBody ApplicationStatusUpdateRequestDto applicationStatusUpdateRequestDto) {
        return updateApplicationStatusPort.updateApplicationStatus(applicationId, applicationStatusUpdateRequestDto).map(body -> new ResponseEntity<>(body, HttpStatus.OK));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER', 'EMPLOYEE')")
    public Mono<Page<ApplicationDto>> getAllApplicationsByVacancyId(@RequestParam UUID vacancyId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return getAllApplicationsByVacancyIdPort.getAllApplicationsByVacancyId(vacancyId, pageable);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<Page<GetMyApplicationResponse>> getApplicationByApplicationId(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return getApplicationPort.getApplicationByApplicationId(pageable);
    }

}
