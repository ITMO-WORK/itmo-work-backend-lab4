package org.itmowork.vacancy_service.application.usecase;


import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.application.dto.ChangeVacancyStatusCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.event.VacancyStatusChangedEvent;
import org.itmowork.vacancy_service.application.port.in.ChangeVacancyStatusUseCase;
import org.itmowork.vacancy_service.application.port.out.*;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.itmowork.vacancy_service.domain.service.VacancyPolicy;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class ChangeVacancyStatusService implements ChangeVacancyStatusUseCase {

    private final VacancyRepositoryPort vacancyRepositoryPort;
    private final VacancyStatusRepositoryPort vacancyStatusRepositoryPort;
    private final CompanyPort companyPort;
    private final CurrentUserPort currentUserPort;
    private final VacancyEventPublisherPort vacancyEventPublisherPort;

    @Override
    public VacancyResult changeStatus(ChangeVacancyStatusCommand command) {
        UUID userId = currentUserPort.getCurrentUserId();

        Vacancy vacancy = vacancyRepositoryPort.findById(command.vacancyId())
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy with id=" + command.vacancyId() + " not found"));

        UUID companyId = vacancy.getCompanyId();
        VacancyStatusName oldStatus = vacancy.getStatus().getVacancyStatusName();
        VacancyStatusName currentStatus = vacancy.getStatus().getVacancyStatusName();
        VacancyStatusName newStatus = command.newStatus();

        if (!companyPort.exists(companyId)) {
            throw new CompanyNotFoundException("Company id not found: " + companyId);
        }
        if (!companyPort.isOwnedBy(companyId, userId)) {
            throw new CompanyNotFoundException("User does not own this company");
        }

        VacancyPolicy.assertTransitionAllowed(currentStatus, newStatus);
        VacancyPolicy.validateSalaryBounds(vacancy.getSalaryFrom(), vacancy.getSalaryTo());

        VacancyStatus statusEntity = vacancyStatusRepositoryPort.findByNameOrThrow(newStatus);
        vacancy.setStatus(statusEntity);

        Vacancy saved = vacancyRepositoryPort.save(vacancy);

        vacancyEventPublisherPort.publishStatusChanged(
                new VacancyStatusChangedEvent(
                        saved.getId(),
                        saved.getCompanyId(),
                        userId,
                        oldStatus,
                        newStatus,
                        Instant.now()
                )
        );

        return new VacancyResult(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getSalaryFrom(),
                saved.getSalaryTo(),
                saved.getStatus().getId(),
                saved.getCompanyId(),
                saved.getCurrency().getId()
        );
    }
}
