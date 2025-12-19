package org.ilestegor.applicationservice.dirty.contoller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.dirty.dto.ApplicationDto;
import org.ilestegor.applicationservice.dirty.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.dirty.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.dirty.dto.response.ApplicationCreateResponseDto;
import org.ilestegor.applicationservice.dirty.dto.response.ApplicationStatusUpdateResponseDto;
import org.ilestegor.applicationservice.dirty.service.interfaces.ApplicationService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(
                                            name = "data",
                                            contentType = MediaType.APPLICATION_JSON_VALUE
                                    )
                            }
                    )
            )
    )
    public Mono<ResponseEntity<ApplicationCreateResponseDto>> createApplication(
            @RequestParam UUID vacancyId,
            @RequestPart("data") @Valid ApplicationCreateRequestDto dto,
            @RequestPart(value = "resume", required = false) FilePart resume,
            @RequestParam(value = "replacedField", required = false) UUID replacedField
    ) {
        return applicationService
                .createApplication(vacancyId, dto, resume, replacedField)
                .map(body -> new ResponseEntity<>(body, HttpStatus.CREATED));
    }

    @PatchMapping("/{vacancyId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<ApplicationCreateResponseDto>> updateApplication(@PathVariable UUID vacancyId, @RequestBody ApplicationCreateRequestDto applicationCreateRequestDto){
        return applicationService.updateApplication(vacancyId, applicationCreateRequestDto).map(body -> new ResponseEntity<>(body, HttpStatus.OK));
    }

    @PatchMapping("/{applicationId}/status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER', 'EMPLOYEE')")
    public Mono<ResponseEntity<ApplicationStatusUpdateResponseDto>> updateApplicationStatus(@PathVariable UUID applicationId, @RequestBody ApplicationStatusUpdateRequestDto applicationStatusUpdateRequestDto){
        return applicationService.updateApplicationStatus(applicationId, applicationStatusUpdateRequestDto).map(body -> new ResponseEntity<>(body, HttpStatus.OK));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER', 'EMPLOYEE')")
    public Mono<Page<ApplicationDto>> getAllApplicationsByVacancyId(@RequestParam UUID vacancyId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return applicationService.getAllApplicationsByVacancyId(vacancyId, pageable);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{applicationId}/resume-url")
    public Mono<ResponseEntity<Map<String, String>>> getResumeUrl(@PathVariable UUID applicationId) {
        return applicationService.getResumeUrlByApplicationId(applicationId)
                .map(url -> ResponseEntity.ok(Map.of("url", url)));
    }

}