package org.itmowork.vacancy_service.application.usecase;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.application.dto.CreateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.port.in.CreateVacancyUseCase;
import org.itmowork.vacancy_service.application.port.out.*;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.itmowork.vacancy_service.domain.service.VacancyPolicy;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateVacancyService implements CreateVacancyUseCase {

    private final VacancyRepositoryPort vacancyRepositoryPort;
    private final VacancyStatusRepositoryPort vacancyStatusRepositoryPort;
    private final CurrencyRepositoryPort currencyRepositoryPort;
    private final CompanyPort companyPort;
    private final CurrentUserPort currentUserPort;

    @Override
    public VacancyResult create(CreateVacancyCommand command, VacancyStatusName initialStatus) {
        UUID userId = currentUserPort.getCurrentUserId();

        if (!companyPort.exists(command.companyId())) {
            throw new CompanyNotFoundException("Company with id " + command.companyId() + " does not exist");
        }

        if (!companyPort.isOwnedBy(command.companyId(), userId)) {
            throw new CompanyNotFoundException("User does not own this company");
        }

        Currency currency = currencyRepositoryPort.findByIdOrThrow(command.currencyId());
        VacancyStatus vacancyStatus = vacancyStatusRepositoryPort.findByNameOrThrow(initialStatus);

        VacancyPolicy.validateSalaryBounds(command.salaryFrom(), command.salaryTo());

        Vacancy vacancy = Vacancy.builder()
                .title(command.title())
                .description(command.description())
                .salaryFrom(command.salaryFrom())
                .salaryTo(command.salaryTo())
                .createdAt(LocalDateTime.now())
                .companyId(command.companyId())
                .status(vacancyStatus)
                .currency(currency)
                .build();

        Vacancy saved = vacancyRepositoryPort.save(vacancy);

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