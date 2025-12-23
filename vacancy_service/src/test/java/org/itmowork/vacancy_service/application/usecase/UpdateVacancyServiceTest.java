package org.itmowork.vacancy_service.application.usecase;

import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.port.out.CompanyPort;
import org.itmowork.vacancy_service.application.port.out.CurrencyRepositoryPort;
import org.itmowork.vacancy_service.application.port.out.CurrentUserPort;
import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.itmowork.vacancy_service.mapper.VacancyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateVacancyServiceTest {

    @Mock VacancyRepositoryPort vacancyRepositoryPort;
    @Mock CurrencyRepositoryPort currencyRepositoryPort;
    @Mock CompanyPort companyPort;
    @Mock CurrentUserPort currentUserPort;
    @Mock VacancyMapper vacancyMapper;

    @InjectMocks UpdateVacancyService service;

    @Test
    void update_happyPath_callsMapper_updatesCurrency_saves_returnsResult() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Currency oldCurrency = Currency.builder().id(10L).currency("EUR").build();
        Currency newCurrency = Currency.builder().id(20L).currency("USD").build();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("old title")
                .description("old desc")
                .salaryFrom(100)
                .salaryTo(200)
                .createdAt(LocalDateTime.now())
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build())
                .currency(oldCurrency)
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(true);

        doNothing().when(vacancyMapper).update(eq(vacancy), any(VacancyUpdateRequestDto.class));

        when(currencyRepositoryPort.findByIdOrThrow(20L)).thenReturn(newCurrency);
        when(vacancyRepositoryPort.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateVacancyCommand cmd = new UpdateVacancyCommand(
                "new title",
                "new desc",
                111,
                222,
                20L
        );

        VacancyResult res = service.update(vacancyId, cmd);

        ArgumentCaptor<VacancyUpdateRequestDto> dtoCaptor = ArgumentCaptor.forClass(VacancyUpdateRequestDto.class);
        verify(vacancyMapper).update(eq(vacancy), dtoCaptor.capture());
        VacancyUpdateRequestDto passedDto = dtoCaptor.getValue();

        assertThat(passedDto.title()).isEqualTo("new title");
        assertThat(passedDto.description()).isEqualTo("new desc");
        assertThat(passedDto.salaryFrom()).isEqualTo(111);
        assertThat(passedDto.salaryTo()).isEqualTo(222);
        assertThat(passedDto.currencyId()).isEqualTo(20L);

        verify(currencyRepositoryPort).findByIdOrThrow(20L);
        assertThat(vacancy.getCurrency().getId()).isEqualTo(20L);

        verify(vacancyRepositoryPort).save(vacancy);

        assertThat(res.id()).isEqualTo(vacancyId);
        assertThat(res.title()).isEqualTo(vacancy.getTitle());
        assertThat(res.description()).isEqualTo(vacancy.getDescription());
        assertThat(res.salaryFrom()).isEqualTo(vacancy.getSalaryFrom());
        assertThat(res.salaryTo()).isEqualTo(vacancy.getSalaryTo());
        assertThat(res.companyId()).isEqualTo(companyId);
        assertThat(res.statusId()).isEqualTo(vacancy.getStatus().getId());
        assertThat(res.currencyId()).isEqualTo(vacancy.getCurrency().getId());
    }

    @Test
    void update_whenCurrencyIdNull_doesNotCallCurrencyRepo() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Currency oldCurrency = Currency.builder().id(10L).currency("EUR").build();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("old title")
                .description("old desc")
                .salaryFrom(100)
                .salaryTo(200)
                .createdAt(LocalDateTime.now())
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build())
                .currency(oldCurrency)
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(true);

        doNothing().when(vacancyMapper).update(eq(vacancy), any(VacancyUpdateRequestDto.class));
        when(vacancyRepositoryPort.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateVacancyCommand cmd = new UpdateVacancyCommand(
                "new title",
                "new desc",
                111,
                222,
                null
        );

        service.update(vacancyId, cmd);

        verify(currencyRepositoryPort, never()).findByIdOrThrow(any());
        verify(vacancyRepositoryPort).save(vacancy);
    }

    @Test
    void update_whenVacancyNotFound_throws() {
        UUID vacancyId = UUID.randomUUID();

        when(currentUserPort.getCurrentUserId()).thenReturn(UUID.randomUUID());
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.empty());

        UpdateVacancyCommand cmd = new UpdateVacancyCommand("t", "d", 1, 2, null);

        assertThatThrownBy(() -> service.update(vacancyId, cmd))
                .isInstanceOf(VacancyNotFoundException.class);

        verifyNoInteractions(companyPort, currencyRepositoryPort, vacancyMapper);
        verify(vacancyRepositoryPort, never()).save(any());
    }

    @Test
    void update_whenCompanyNotExists_throws() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("t").description("d")
                .salaryFrom(100).salaryTo(200)
                .createdAt(LocalDateTime.now())
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build())
                .currency(Currency.builder().id(10L).currency("EUR").build())
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(false);

        UpdateVacancyCommand cmd = new UpdateVacancyCommand("t", "d", 1, 2, null);

        assertThatThrownBy(() -> service.update(vacancyId, cmd))
                .isInstanceOf(CompanyNotFoundException.class);

        verify(vacancyMapper, never()).update(any(), any());
        verify(currencyRepositoryPort, never()).findByIdOrThrow(any());
        verify(vacancyRepositoryPort, never()).save(any());
    }

    @Test
    void update_whenNotOwned_throws() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("t").description("d")
                .salaryFrom(100).salaryTo(200)
                .createdAt(LocalDateTime.now())
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build())
                .currency(Currency.builder().id(10L).currency("EUR").build())
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(false);

        UpdateVacancyCommand cmd = new UpdateVacancyCommand("t", "d", 1, 2, null);

        assertThatThrownBy(() -> service.update(vacancyId, cmd))
                .isInstanceOf(CompanyNotFoundException.class);

        verify(vacancyMapper, never()).update(any(), any());
        verify(currencyRepositoryPort, never()).findByIdOrThrow(any());
        verify(vacancyRepositoryPort, never()).save(any());
    }
}
