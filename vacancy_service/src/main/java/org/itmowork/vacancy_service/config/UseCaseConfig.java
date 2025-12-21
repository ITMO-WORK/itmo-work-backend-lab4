package org.itmowork.vacancy_service.config;

import org.itmowork.vacancy_service.application.port.in.*;
import org.itmowork.vacancy_service.application.port.out.*;
import org.itmowork.vacancy_service.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public VacancyQueriesUseCase vacancyQueriesUseCase(VacancyRepositoryPort vacancyRepositoryPort) {
        return new VacancyQueriesService(vacancyRepositoryPort);
    }

    @Bean
    public GetPublishedVacanciesUseCase getPublishedVacanciesUseCase(
            VacancyRepositoryPort vacancyRepositoryPort
    ) {
        return new GetPublishedVacanciesService(vacancyRepositoryPort);
    }

    @Bean
    public UpdateVacancyAndChangeStatusUseCase updateVacancyAndChangeStatusUseCase(
            VacancyRepositoryPort vacancyRepositoryPort,
            VacancyStatusRepositoryPort vacancyStatusRepositoryPort,
            CurrencyRepositoryPort currencyRepositoryPort,
            CompanyPort companyPort,
            CurrentUserPort currentUserPort,
            VacancyEventPublisherPort vacancyEventPublisherPort,
            org.itmowork.vacancy_service.mapper.VacancyMapper vacancyMapper
    ) {
        return new UpdateVacancyAndChangeStatusService(
                vacancyRepositoryPort,
                vacancyStatusRepositoryPort,
                currencyRepositoryPort,
                companyPort,
                currentUserPort,
                vacancyEventPublisherPort,
                vacancyMapper
        );
    }

    @Bean
    public UpdateVacancyUseCase updateVacancyUseCase(
            VacancyRepositoryPort vacancyRepositoryPort,
            CurrencyRepositoryPort currencyRepositoryPort,
            CompanyPort companyPort,
            CurrentUserPort currentUserPort,
            org.itmowork.vacancy_service.mapper.VacancyMapper vacancyMapper
    ) {
        return new UpdateVacancyService(
                vacancyRepositoryPort,
                currencyRepositoryPort,
                companyPort,
                currentUserPort,
                vacancyMapper
        );
    }

    @Bean
    public ChangeVacancyStatusUseCase changeVacancyStatusUseCase(
            VacancyRepositoryPort vacancyRepositoryPort,
            VacancyStatusRepositoryPort vacancyStatusRepositoryPort,
            CompanyPort companyPort,
            CurrentUserPort currentUserPort,
            VacancyEventPublisherPort vacancyEventPublisherPort
    ) {
        return new ChangeVacancyStatusService(
                vacancyRepositoryPort,
                vacancyStatusRepositoryPort,
                companyPort,
                currentUserPort,
                vacancyEventPublisherPort
        );
    }

    @Bean
    public CreateVacancyUseCase createVacancyUseCase(
            VacancyRepositoryPort vacancyRepositoryPort,
            VacancyStatusRepositoryPort vacancyStatusRepositoryPort,
            CurrencyRepositoryPort currencyRepositoryPort,
            CompanyPort companyPort,
            CurrentUserPort currentUserPort
    ) {
        return new CreateVacancyService(
                vacancyRepositoryPort,
                vacancyStatusRepositoryPort,
                currencyRepositoryPort,
                companyPort,
                currentUserPort
        );
    }
}
