package org.itmowork.vacancy_service.application.usecase;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyAndChangeStatusCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.event.VacancyStatusChangedEvent;
import org.itmowork.vacancy_service.application.port.in.UpdateVacancyAndChangeStatusUseCase;
import org.itmowork.vacancy_service.application.port.out.*;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.exception.exceptions.InvalidVacancyStatusChangeException;
import org.itmowork.vacancy_service.domain.exception.exceptions.InvalidVacancyStatusException;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.itmowork.vacancy_service.domain.service.VacancyPolicy;
import org.itmowork.vacancy_service.mapper.VacancyMapper;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdateVacancyAndChangeStatusService implements UpdateVacancyAndChangeStatusUseCase {

    private final VacancyRepositoryPort vacancyRepositoryPort;
    private final VacancyStatusRepositoryPort vacancyStatusRepositoryPort;
    private final CurrencyRepositoryPort currencyRepositoryPort;
    private final CompanyPort companyPort;
    private final CurrentUserPort currentUserPort;
    private final VacancyEventPublisherPort vacancyEventPublisherPort;

    private final VacancyMapper vacancyMapper;

    @Override
    @Transactional
    public VacancyResult updateAndChangeStatus(UpdateVacancyAndChangeStatusCommand command) {
        UUID userId = currentUserPort.getCurrentUserId();

        UUID vacancyId = command.vacancyId();
        VacancyStatusName newStatus = command.newStatus();

        Vacancy vacancy = vacancyRepositoryPort.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found"));

        UUID companyId = vacancy.getCompanyId();
        VacancyStatusName currentStatus = vacancy.getStatus().getVacancyStatusName();

        if (!companyPort.exists(companyId)) {
            throw new CompanyNotFoundException("Company id not found: " + companyId);
        }
        if (!companyPort.isOwnedBy(companyId, userId)) {
            throw new CompanyNotFoundException("User does not own this company");
        }

        VacancyPolicy.assertUpdateAllowed(currentStatus);
        VacancyPolicy.assertTransitionAllowed(currentStatus, newStatus);

        var u = command.update();
        VacancyUpdateRequestDto dtoForMapper = new VacancyUpdateRequestDto(
                u.title(),
                u.description(),
                u.salaryFrom(),
                u.salaryTo(),
                u.currencyId()
        );
        vacancyMapper.update(vacancy, dtoForMapper);

        if (u.currencyId() != null) {
            Currency currency = currencyRepositoryPort.findByIdOrThrow(u.currencyId());
            vacancy.setCurrency(currency);
        }

        VacancyPolicy.validateSalaryBounds(vacancy.getSalaryFrom(), vacancy.getSalaryTo());

        VacancyStatus statusEntity = vacancyStatusRepositoryPort.findByNameOrThrow(newStatus);
        vacancy.setStatus(statusEntity);

        Vacancy saved = vacancyRepositoryPort.save(vacancy);

        vacancyEventPublisherPort.publishStatusChanged(
                new VacancyStatusChangedEvent(
                        saved.getId(),
                        saved.getCompanyId(),
                        userId,
                        currentStatus,
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
