package org.itmowork.vacancy_service.application.usecase;

import org.itmowork.vacancy_service.application.dto.ChangeVacancyStatusCommand;
import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.event.VacancyStatusChangedEvent;
import org.itmowork.vacancy_service.application.port.out.*;
import org.itmowork.vacancy_service.application.usecase.ChangeVacancyStatusService;
import org.itmowork.vacancy_service.domain.exception.exceptions.CompanyNotFoundException;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeVacancyStatusServiceTest {

    @Mock VacancyRepositoryPort vacancyRepositoryPort;
    @Mock VacancyStatusRepositoryPort vacancyStatusRepositoryPort;
    @Mock CompanyPort companyPort;
    @Mock CurrentUserPort currentUserPort;
    @Mock VacancyEventPublisherPort vacancyEventPublisherPort;

    @InjectMocks
    ChangeVacancyStatusService service;

    @Test
    void changeStatus_happyPath_updatesStatus_saves_publishesEvent_returnsResult() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Long oldStatusId = 1L;
        Long newStatusId = 2L;
        Long currencyId = 10L;

        VacancyStatus oldStatus = VacancyStatus.builder()
                .id(oldStatusId)
                .vacancyStatusName(VacancyStatusName.DRAFT)
                .build();

        VacancyStatus newStatus = VacancyStatus.builder()
                .id(newStatusId)
                .vacancyStatusName(VacancyStatusName.PUBLISHED)
                .build();

        Currency currency = Currency.builder().id(currencyId).currency("EUR").build();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .title("t")
                .description("d")
                .salaryFrom(100)
                .salaryTo(200)
                .status(oldStatus)
                .currency(currency)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(true);
        when(vacancyStatusRepositoryPort.findByNameOrThrow(VacancyStatusName.PUBLISHED)).thenReturn(newStatus);
        when(vacancyRepositoryPort.save(any(Vacancy.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        VacancyResult result = service.changeStatus(new ChangeVacancyStatusCommand(vacancyId, VacancyStatusName.PUBLISHED));
        Instant after = Instant.now();

        ArgumentCaptor<Vacancy> savedCaptor = ArgumentCaptor.forClass(Vacancy.class);
        verify(vacancyRepositoryPort).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getStatus().getVacancyStatusName()).isEqualTo(VacancyStatusName.PUBLISHED);

        ArgumentCaptor<VacancyStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(VacancyStatusChangedEvent.class);
        verify(vacancyEventPublisherPort).publishStatusChanged(eventCaptor.capture());
        VacancyStatusChangedEvent event = eventCaptor.getValue();

        assertThat(event.vacancyId()).isEqualTo(vacancyId);
        assertThat(event.companyId()).isEqualTo(companyId);
        assertThat(event.actorUserId()).isEqualTo(userId);
        assertThat(event.oldStatus()).isEqualTo(VacancyStatusName.DRAFT);
        assertThat(event.newStatus()).isEqualTo(VacancyStatusName.PUBLISHED);
        assertThat(event.occurredAt()).isBetween(before, after);

        assertThat(result.id()).isEqualTo(vacancyId);
        assertThat(result.companyId()).isEqualTo(companyId);
        assertThat(result.statusId()).isEqualTo(newStatusId);
        assertThat(result.currencyId()).isEqualTo(currencyId);
    }

    @Test
    void changeStatus_whenVacancyNotFound_throwsVacancyNotFound() {
        UUID vacancyId = UUID.randomUUID();
        when(currentUserPort.getCurrentUserId()).thenReturn(UUID.randomUUID());
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.changeStatus(new ChangeVacancyStatusCommand(vacancyId, VacancyStatusName.PUBLISHED))
        ).isInstanceOf(VacancyNotFoundException.class);

        verifyNoInteractions(companyPort, vacancyStatusRepositoryPort, vacancyEventPublisherPort);
        verify(vacancyRepositoryPort, never()).save(any());
    }

    @Test
    void changeStatus_whenCompanyNotExists_throwsCompanyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build())
                .currency(Currency.builder().id(10L).currency("EUR").build())
                .salaryFrom(100).salaryTo(200)
                .title("t").description("d")
                .createdAt(java.time.LocalDateTime.now())
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(false);

        assertThatThrownBy(() ->
                service.changeStatus(new ChangeVacancyStatusCommand(vacancyId, VacancyStatusName.PUBLISHED))
        ).isInstanceOf(CompanyNotFoundException.class);

        verify(vacancyRepositoryPort, never()).save(any());
        verifyNoInteractions(vacancyStatusRepositoryPort, vacancyEventPublisherPort);
    }

    @Test
    void changeStatus_whenNotOwned_throwsCompanyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Vacancy vacancy = Vacancy.builder()
                .id(vacancyId)
                .companyId(companyId)
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.DRAFT).build())
                .currency(Currency.builder().id(10L).currency("EUR").build())
                .salaryFrom(100).salaryTo(200)
                .title("t").description("d")
                .createdAt(java.time.LocalDateTime.now())
                .build();

        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(vacancyRepositoryPort.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyPort.exists(companyId)).thenReturn(true);
        when(companyPort.isOwnedBy(companyId, userId)).thenReturn(false);

        assertThatThrownBy(() ->
                service.changeStatus(new ChangeVacancyStatusCommand(vacancyId, VacancyStatusName.PUBLISHED))
        ).isInstanceOf(CompanyNotFoundException.class);

        verify(vacancyRepositoryPort, never()).save(any());
        verifyNoInteractions(vacancyStatusRepositoryPort, vacancyEventPublisherPort);
    }
}

