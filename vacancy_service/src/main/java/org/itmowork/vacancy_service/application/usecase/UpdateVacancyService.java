package org.itmowork.vacancy_service.application.usecase;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.port.in.UpdateVacancyUseCase;
import org.itmowork.vacancy_service.application.port.out.CompanyPort;
import org.itmowork.vacancy_service.application.port.out.CurrencyRepositoryPort;
import org.itmowork.vacancy_service.application.port.out.CurrentUserPort;
import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.exception.exceptions.InvalidVacancyStatusException;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.itmowork.vacancy_service.domain.service.VacancyPolicy;
import org.itmowork.vacancy_service.mapper.VacancyMapper;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateVacancyService implements UpdateVacancyUseCase {

    private final VacancyRepositoryPort vacancyRepositoryPort;
    private final CurrencyRepositoryPort currencyRepositoryPort;
    private final CompanyPort companyPort;
    private final CurrentUserPort currentUserPort;

    private final VacancyMapper vacancyMapper; // MapStruct bean, НЕ меняем

    @Override
    public VacancyResult update(UUID vacancyId, UpdateVacancyCommand command) {
        UUID userId = currentUserPort.getCurrentUserId();

        Vacancy vacancy = vacancyRepositoryPort.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found"));

        UUID companyId = vacancy.getCompanyId();

        if (!companyPort.exists(companyId)) {
            throw new CompanyNotFoundException("Company not found: " + companyId);
        }
        if (!companyPort.isOwnedBy(companyId, userId)) {
            throw new CompanyNotFoundException("User does not own this company");
        }

        VacancyStatusName status = vacancy.getStatus().getVacancyStatusName();
        VacancyPolicy.assertUpdateAllowed(status);

        VacancyUpdateRequestDto dtoForMapper = new VacancyUpdateRequestDto(
                command.title(),
                command.description(),
                command.salaryFrom(),
                command.salaryTo(),
                command.currencyId()
        );
        vacancyMapper.update(vacancy, dtoForMapper);

        if (command.currencyId() != null) {
            Currency currency = currencyRepositoryPort.findByIdOrThrow(command.currencyId());
            vacancy.setCurrency(currency);
        }

        VacancyPolicy.validateSalaryBounds(vacancy.getSalaryFrom(), vacancy.getSalaryTo());

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