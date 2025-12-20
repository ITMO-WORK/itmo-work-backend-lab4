package org.itmowork.vacancy_service.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyCreateRequestDto;
import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.adapter.in.web.dto.response.VacancyResponseDto;
import org.itmowork.vacancy_service.adapter.in.web.mapper.VacancyCreateWebMapper;
import org.itmowork.vacancy_service.adapter.in.web.mapper.VacancyStatusWebMapper;
import org.itmowork.vacancy_service.adapter.in.web.mapper.VacancyUpdateAndChangeStatusWebMapper;
import org.itmowork.vacancy_service.adapter.in.web.mapper.VacancyUpdateWebMapper;
import org.itmowork.vacancy_service.application.port.in.*;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final CreateVacancyUseCase createVacancyUseCase;
    private final VacancyCreateWebMapper vacancyCreateWebMapper;
    private final ChangeVacancyStatusUseCase changeVacancyStatusUseCase;
    private final VacancyStatusWebMapper vacancyStatusWebMapper;
    private final UpdateVacancyUseCase updateVacancyUseCase;
    private final VacancyUpdateWebMapper vacancyUpdateWebMapper;
    private final UpdateVacancyAndChangeStatusUseCase updateVacancyAndChangeStatusUseCase;
    private final VacancyUpdateAndChangeStatusWebMapper vacancyUpdateAndChangeStatusWebMapper;
    private final GetPublishedVacanciesUseCase getPublishedVacanciesUseCase;
    private final VacancyQueriesUseCase vacancyQueriesUseCase;

    @GetMapping
    public PagedModel<VacancyResponseDto> getAllPublishedVacancies(Pageable pageable) {
        var page = getPublishedVacanciesUseCase.getPublished(
                new org.itmowork.vacancy_service.application.dto.query.GetPublishedVacanciesQuery(pageable)
        );

        var mapped = page.map(vacancyCreateWebMapper::toResponse);
        return new PagedModel<>(mapped);
    }

    @PatchMapping("/{id}/update-and-change-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VacancyResponseDto> updateAndChangeStatus(
            @PathVariable UUID id,
            @RequestBody @Valid VacancyUpdateRequestDto dto,
            @RequestParam VacancyStatusName newStatus
    ) {
        var command = vacancyUpdateAndChangeStatusWebMapper.toCommand(id, dto, newStatus);
        var result = updateVacancyAndChangeStatusUseCase.updateAndChangeStatus(command);
        return ResponseEntity.ok(vacancyCreateWebMapper.toResponse(result));
    }

    @PatchMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VacancyResponseDto> updateVacancy(
            @PathVariable UUID id,
            @RequestBody @Valid VacancyUpdateRequestDto dto
    ) {
        var command = vacancyUpdateWebMapper.toCommand(dto);
        var result = updateVacancyUseCase.update(id, command);
        return ResponseEntity.ok(vacancyCreateWebMapper.toResponse(result)); // переиспользуем toResponse
    }

    @PatchMapping("/{id}/change-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VacancyResponseDto> changeStatus(
            @PathVariable UUID id,
            @RequestParam VacancyStatusName newStatus
    ) {
        var command = vacancyStatusWebMapper.toCommand(id, newStatus);
        var result = changeVacancyStatusUseCase.changeStatus(command);
        return ResponseEntity.ok(vacancyCreateWebMapper.toResponse(result));
    }

    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VacancyResponseDto> createPublish(
            @RequestBody @Valid VacancyCreateRequestDto request
    ) {
        var command = vacancyCreateWebMapper.toCommand(request);
        var result = createVacancyUseCase.create(command, VacancyStatusName.PUBLISHED);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyCreateWebMapper.toResponse(result));
    }

    @PostMapping("/draft")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VacancyResponseDto> createDraft(
            @RequestBody @Valid VacancyCreateRequestDto request
    ) {
        var command = vacancyCreateWebMapper.toCommand(request);
        var result = createVacancyUseCase.create(command, VacancyStatusName.DRAFT);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyCreateWebMapper.toResponse(result));
    }

    @GetMapping("/{vacancyId}/company-id")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<UUID> getCompanyIdByVacancy(@PathVariable UUID vacancyId) {
        return ResponseEntity.ok(vacancyQueriesUseCase.getCompanyId(vacancyId));
    }

    @GetMapping("/{id}/title")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'COMPANY_OWNER', 'EMPLOYEE')")
    public ResponseEntity<String> getVacancyTitle(@PathVariable UUID id) {
        return ResponseEntity.ok(vacancyQueriesUseCase.getTitle(id));
    }

    @GetMapping("/{id}/is-published")
    public ResponseEntity<Boolean> isVacancyPublished(@PathVariable UUID id) {
        return ResponseEntity.ok(vacancyQueriesUseCase.isPublished(id));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable UUID id) {
        return ResponseEntity.ok(vacancyQueriesUseCase.exists(id));
    }

}
