package org.itmowork.vacancy_service.application.usecase;

import org.itmowork.vacancy_service.adapter.in.web.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyAndChangeStatusCommand;
import org.itmowork.vacancy_service.application.dto.UpdateVacancyCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.event.VacancyStatusChangedEvent;
import org.itmowork.vacancy_service.application.port.out.*;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateVacancyAndChangeStatusServiceTest {

    @Mock VacancyRepositoryPort vacancyRepositoryPort;
    @Mock VacancyStatusRepositoryPort vacancyStatusRepositoryPort;
    @Mock CurrencyRepositoryPort currencyRepositoryPort;
    @Mock CompanyPort companyPort;
    @Mock CurrentUserPort currentUserPort;
    @Mock VacancyEventPublisherPort vacancyEventPublisherPort;
    @Mock VacancyMapper vacancyMapper;

    @InjectMocks UpdateVacancyAndChangeStatusService service;

    @Test
    void updateAndChangeStatus_happyPath_callsMapper_updatesCurrency_changesStatus_saves_publishes() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Currency oldCurrency = Currency.builder().id(10L).currency("EUR").build();
        Currency newCurrency = Currency.builder().id(20L).currency("USD").build();

        VacancyStatus draft = VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build();
        VacancyStatus published = VacancyStatus.builder().id(2L).vacancyStatusName(VacancyStatusName.PUBLISHED).build();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("old")
                .description("old")
                .salaryFrom(100)
                .salaryTo(200)
                .createdAt(LocalDateTime.now())
                .status(draft)
                .currency(oldCurrency)
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));

        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(true);

        doNothing().when(vacancyMapper).update(eq(vacancy), any(VacancyUpdateRequestDto.class));

        when(currencyRepositoryPort.findByIdOrThrow(20L)).thenReturn(newCurrency);
        when(vacancyStatusRepositoryPort.findByNameOrThrow(VacancyStatusName.PUBLISHED)).thenReturn(published);
        when(vacancyRepositoryPort.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateVacancyCommand upd = new UpdateVacancyCommand(
                "new",
                "new",
                111,
                222,
                20L
        );
        UpdateVacancyAndChangeStatusCommand cmd =
                new UpdateVacancyAndChangeStatusCommand(vacancyId, upd, VacancyStatusName.PUBLISHED);

        Instant before = Instant.now();
        VacancyResult res = service.updateAndChangeStatus(cmd);
        Instant after = Instant.now();

        ArgumentCaptor<VacancyUpdateRequestDto> dtoCaptor = ArgumentCaptor.forClass(VacancyUpdateRequestDto.class);
        verify(vacancyMapper).update(eq(vacancy), dtoCaptor.capture());
        VacancyUpdateRequestDto passedDto = dtoCaptor.getValue();

        assertThat(passedDto.title()).isEqualTo("new");
        assertThat(passedDto.description()).isEqualTo("new");
        assertThat(passedDto.salaryFrom()).isEqualTo(111);
        assertThat(passedDto.salaryTo()).isEqualTo(222);
        assertThat(passedDto.currencyId()).isEqualTo(20L);

        verify(currencyRepositoryPort).findByIdOrThrow(20L);
        assertThat(vacancy.getCurrency().getId()).isEqualTo(20L);

        verify(vacancyStatusRepositoryPort).findByNameOrThrow(VacancyStatusName.PUBLISHED);
        assertThat(vacancy.getStatus().getVacancyStatusName()).isEqualTo(VacancyStatusName.PUBLISHED);
        assertThat(vacancy.getStatus().getId()).isEqualTo(2L);

        verify(vacancyRepositoryPort).save(vacancy);

        ArgumentCaptor<VacancyStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(VacancyStatusChangedEvent.class);
        verify(vacancyEventPublisherPort).publishStatusChanged(eventCaptor.capture());
        VacancyStatusChangedEvent ev = eventCaptor.getValue();

        assertThat(ev.vacancyId()).isEqualTo(vacancyId);
        assertThat(ev.companyId()).isEqualTo(companyId);
        assertThat(ev.actorUserId()).isEqualTo(userId);
        assertThat(ev.oldStatus()).isEqualTo(VacancyStatusName.DRAFT);
        assertThat(ev.newStatus()).isEqualTo(VacancyStatusName.PUBLISHED);
        assertThat(ev.occurredAt()).isBetween(before, after);

        assertThat(res.id()).isEqualTo(vacancyId);
        assertThat(res.companyId()).isEqualTo(companyId);
        assertThat(res.statusId()).isEqualTo(2L);
        assertThat(res.currencyId()).isEqualTo(20L);
    }

    @Test
    void updateAndChangeStatus_whenCurrencyIdNull_doesNotCallCurrencyRepo() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Currency oldCurrency = Currency.builder().id(10L).currency("EUR").build();

        VacancyStatus draft = VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build();
        VacancyStatus published = VacancyStatus.builder().id(2L).vacancyStatusName(VacancyStatusName.PUBLISHED).build();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("old")
                .description("old")
                .salaryFrom(100)
                .salaryTo(200)
                .createdAt(LocalDateTime.now())
                .status(draft)
                .currency(oldCurrency)
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(true);

        doNothing().when(vacancyMapper).update(eq(vacancy), any(VacancyUpdateRequestDto.class));

        when(vacancyStatusRepositoryPort.findByNameOrThrow(VacancyStatusName.PUBLISHED)).thenReturn(published);
        when(vacancyRepositoryPort.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateVacancyCommand upd = new UpdateVacancyCommand(
                "new",
                "new",
                111,
                222,
                null
        );
        UpdateVacancyAndChangeStatusCommand cmd =
                new UpdateVacancyAndChangeStatusCommand(vacancyId, upd, VacancyStatusName.PUBLISHED);

        service.updateAndChangeStatus(cmd);

        verify(currencyRepositoryPort, never()).findByIdOrThrow(any());
        verify(vacancyRepositoryPort).save(vacancy);
    }

    @Test
    void updateAndChangeStatus_whenVacancyNotFound_throws() {
        UUID vacancyId = UUID.randomUUID();
        when(currentUserPort.getCurrentUserId()).thenReturn(UUID.randomUUID());
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.empty());

        UpdateVacancyCommand upd = new UpdateVacancyCommand("t", "d", 1, 2, null);
        UpdateVacancyAndChangeStatusCommand cmd =
                new UpdateVacancyAndChangeStatusCommand(vacancyId, upd, VacancyStatusName.PUBLISHED);

        assertThatThrownBy(() -> service.updateAndChangeStatus(cmd))
                .isInstanceOf(VacancyNotFoundException.class);

        verifyNoInteractions(companyPort, vacancyStatusRepositoryPort, currencyRepositoryPort, vacancyEventPublisherPort, vacancyMapper);
        verify(vacancyRepositoryPort, never()).save(any());
    }

    @Test
    void updateAndChangeStatus_whenCompanyNotExists_throws() {
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

        UpdateVacancyCommand upd = new UpdateVacancyCommand("t", "d", 1, 2, null);
        UpdateVacancyAndChangeStatusCommand cmd =
                new UpdateVacancyAndChangeStatusCommand(vacancyId, upd, VacancyStatusName.PUBLISHED);

        assertThatThrownBy(() -> service.updateAndChangeStatus(cmd))
                .isInstanceOf(CompanyNotFoundException.class);

        verify(vacancyMapper, never()).update(any(), any());
        verify(currencyRepositoryPort, never()).findByIdOrThrow(any());
        verify(vacancyStatusRepositoryPort, never()).findByNameOrThrow(any());
        verify(vacancyEventPublisherPort, never()).publishStatusChanged(any());
        verify(vacancyRepositoryPort, never()).save(any());
    }

    @Test
    void updateAndChangeStatus_whenNotOwned_throws() {
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

        UpdateVacancyCommand upd = new UpdateVacancyCommand("t", "d", 1, 2, null);
        UpdateVacancyAndChangeStatusCommand cmd =
                new UpdateVacancyAndChangeStatusCommand(vacancyId, upd, VacancyStatusName.PUBLISHED);

        assertThatThrownBy(() -> service.updateAndChangeStatus(cmd))
                .isInstanceOf(CompanyNotFoundException.class);

        verify(vacancyMapper, never()).update(any(), any());
        verify(currencyRepositoryPort, never()).findByIdOrThrow(any());
        verify(vacancyStatusRepositoryPort, never()).findByNameOrThrow(any());
        verify(vacancyEventPublisherPort, never()).publishStatusChanged(any());
        verify(vacancyRepositoryPort, never()).save(any());
    }
}
