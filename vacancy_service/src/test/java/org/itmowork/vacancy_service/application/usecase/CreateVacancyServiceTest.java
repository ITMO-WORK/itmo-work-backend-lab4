package org.itmowork.vacancy_service.application.usecase;

import org.itmowork.vacancy_service.application.dto.CreateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.port.out.*;
import org.itmowork.vacancy_service.application.usecase.CreateVacancyService;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateVacancyServiceTest {

    @Mock VacancyRepositoryPort vacancyRepositoryPort;
    @Mock VacancyStatusRepositoryPort vacancyStatusRepositoryPort;
    @Mock CurrencyRepositoryPort currencyRepositoryPort;
    @Mock CompanyPort companyPort;
    @Mock CurrentUserPort currentUserPort;

    @InjectMocks
    CreateVacancyService service;

    @Test
    void create_happyPath_buildsVacancy_saves_returnsResult() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Long currencyId = 10L;

        Currency currency = Currency.builder().id(currencyId).currency("EUR").build();

        Long statusId = 1L;
        VacancyStatus status = VacancyStatus.builder()
                .id(statusId)
                .vacancyStatusName(VacancyStatusName.DRAFT)
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(true);
        when(currencyRepositoryPort.findByIdOrThrow(currencyId)).thenReturn(currency);
        when(vacancyStatusRepositoryPort.findByNameOrThrow(VacancyStatusName.DRAFT)).thenReturn(status);

        UUID generatedId = UUID.randomUUID();
        when(vacancyRepositoryPort.save(any(Vacancy.class))).thenAnswer(inv -> {
            Vacancy v = inv.getArgument(0);
            v.setId(generatedId);
            return v;
        });

        CreateVacancyCommand cmd = new CreateVacancyCommand(
                "Title",
                "Desc",
                100,
                200,
                companyId,
                currencyId
        );

        VacancyResult result = service.create(cmd, VacancyStatusName.DRAFT);

        ArgumentCaptor<Vacancy> captor = ArgumentCaptor.forClass(Vacancy.class);
        verify(vacancyRepositoryPort).save(captor.capture());
        Vacancy toSave = captor.getValue();

        assertThat(toSave.getTitle()).isEqualTo("Title");
        assertThat(toSave.getDescription()).isEqualTo("Desc");
        assertThat(toSave.getSalaryFrom()).isEqualTo(100);
        assertThat(toSave.getSalaryTo()).isEqualTo(200);
        assertThat(toSave.getCompanyId()).isEqualTo(companyId);
        assertThat(toSave.getCurrency().getId()).isEqualTo(currencyId);
        assertThat(toSave.getStatus().getVacancyStatusName()).isEqualTo(VacancyStatusName.DRAFT);
        assertThat(toSave.getCreatedAt()).isNotNull();

        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.companyId()).isEqualTo(companyId);
        assertThat(result.currencyId()).isEqualTo(currencyId);
        assertThat(result.statusId()).isEqualTo(statusId);
    }

    @Test
    void create_whenCompanyNotExists_throws() {
        UUID companyId = UUID.randomUUID();
        when(currentUserPort.getCurrentUserId()).thenReturn(UUID.randomUUID());
        when(companyPort.exists(companyId)).thenReturn(false);

        CreateVacancyCommand cmd = new CreateVacancyCommand("t", "d", 1, 2, companyId, 10L);

        assertThatThrownBy(() -> service.create(cmd, VacancyStatusName.DRAFT))
                .isInstanceOf(CompanyNotFoundException.class);

        verifyNoInteractions(currencyRepositoryPort, vacancyStatusRepositoryPort);
        verify(vacancyRepositoryPort, never()).save(any());
    }

    @Test
    void create_whenNotOwned_throws() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(false);

        CreateVacancyCommand cmd = new CreateVacancyCommand("t", "d", 1, 2, companyId, 10L);

        assertThatThrownBy(() -> service.create(cmd, VacancyStatusName.DRAFT))
                .isInstanceOf(CompanyNotFoundException.class);

        verifyNoInteractions(currencyRepositoryPort, vacancyStatusRepositoryPort);
        verify(vacancyRepositoryPort, never()).save(any());
    }
}
