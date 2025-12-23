package org.ilestegor.applicationservice.application.usecase;

import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationNotFoundException;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationStatusNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateApplicationStatusUseCaseTest {

    @Captor
    ArgumentCaptor<Application> appCaptor;
    @Mock
    private CurrentUserPort currentUserPort;
    @Mock
    private ApplicationPreconditions preconditions;
    @Mock
    private ApplicationRepositoryPort applicationRepositoryPort;
    @Mock
    private ApplicationStatusRepositoryPort applicationStatusRepositoryPort;
    @Mock
    private ApplicationEventPublisherPort applicationEventPublisherPort;
    @Mock
    private VacancyPort vacancyPort;
    @InjectMocks
    private UpdateApplicationStatusUseCase useCase;
    private UUID applicationId;
    private UUID vacancyId;
    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        vacancyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        token = "jwt-token";
    }


    private Application appWith(UUID appId, UUID vId, UUID uId, long statusId, LocalDateTime updatedAt) {
        return Application.builder()
                .id(appId)
                .vacancyId(vId)
                .userId(uId)
                .status(statusId)
                .coverLetter("cover")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(updatedAt)
                .build();
    }

    private ApplicationStatus status(long id, ApplicationStatusName name) {
        var st = new ApplicationStatus();
        st.setId(id);
        st.setApplicationStatusName(name);
        return st;
    }

    @Test
    void updateApplicationStatus_checkUserExistsError_shouldPropagate() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));

        RuntimeException err = new RuntimeException("user not found");
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.error(err));


        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.NEW)))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();

        verify(currentUserPort).getCurrentUser();
        verify(preconditions).checkUserExists(userId, token);
        verify(applicationRepositoryPort).findById(applicationId);

        verifyNoInteractions(applicationStatusRepositoryPort, applicationEventPublisherPort, vacancyPort);
        verify(preconditions, never()).checkVacancyExists(any(), any());
        verify(preconditions, never()).checkUserBelongsToCompany(any(), any(), any());
    }


    @Test
    void updateApplicationStatus_applicationNotFound_shouldError() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.NEW)))
                .expectError(ApplicationNotFoundException.class)
                .verify();

        verify(applicationRepositoryPort).findById(applicationId);
        verifyNoInteractions(applicationStatusRepositoryPort, applicationEventPublisherPort, vacancyPort);
    }

    @Test
    void updateApplicationStatus_vacancyIdNull_andFindVacancyIdEmpty_shouldError() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = appWith(applicationId, null, userId, 10L, LocalDateTime.now().minusHours(3));
        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.just(app));
        when(applicationRepositoryPort.findVacancyIdById(applicationId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.NEW)))
                .expectError(ApplicationNotFoundException.class)
                .verify();

        verify(applicationRepositoryPort).findById(applicationId);
        verify(applicationRepositoryPort).findVacancyIdById(applicationId);

        verifyNoInteractions(applicationStatusRepositoryPort, applicationEventPublisherPort, vacancyPort);
        verify(preconditions, never()).checkVacancyExists(any(), any());
    }

    @Test
    void updateApplicationStatus_authorizationFails_shouldPropagate() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = appWith(applicationId, vacancyId, userId, 10L, LocalDateTime.now().minusHours(3));
        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.just(app));

        RuntimeException authErr = new RuntimeException("forbidden");
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.error(authErr));


        when(preconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());


        when(applicationStatusRepositoryPort.findById(anyLong()))
                .thenReturn(Mono.just(status(10L, ApplicationStatusName.NEW)));
        when(applicationStatusRepositoryPort.findByName(any()))
                .thenReturn(Mono.just(status(20L, ApplicationStatusName.REJECTED)));
        when(vacancyPort.getVacancyTitle(any(), any()))
                .thenReturn(Mono.just("title"));

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.REJECTED)))
                .expectErrorSatisfies(e -> assertSame(authErr, e))
                .verify();

        verify(preconditions).checkVacancyExists(vacancyId, token);
        verify(preconditions).checkUserBelongsToCompany(vacancyId, userId, token);

        verify(applicationRepositoryPort, never()).save(any());
        verifyNoInteractions(applicationEventPublisherPort);
    }

    @Test
    void updateApplicationStatus_oldStatusNotFound_shouldError() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = appWith(applicationId, vacancyId, userId, 111L, LocalDateTime.now().minusHours(3));
        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.just(app));

        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());

        when(applicationStatusRepositoryPort.findById(111L)).thenReturn(Mono.empty());
        when(applicationStatusRepositoryPort.findByName(any()))
                .thenReturn(Mono.just(status(222L, ApplicationStatusName.REJECTED)));
        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("title"));

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.REJECTED)))
                .expectError(ApplicationStatusNotFoundException.class)
                .verify();

        verify(applicationRepositoryPort, never()).save(any());
        verifyNoInteractions(applicationEventPublisherPort);
    }


    @Test
    void updateApplicationStatus_newStatusNotFound_shouldError() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = appWith(applicationId, vacancyId, userId, 111L, LocalDateTime.now().minusHours(3));
        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.just(app));

        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());

        when(applicationStatusRepositoryPort.findById(111L))
                .thenReturn(Mono.just(status(111L, ApplicationStatusName.NEW)));
        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.REJECTED)).thenReturn(Mono.empty());
        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("title"));

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.REJECTED)))
                .expectError(ApplicationStatusNotFoundException.class)
                .verify();

        verify(applicationRepositoryPort, never()).save(any());
        verifyNoInteractions(applicationEventPublisherPort);
    }

    @Test
    void updateApplicationStatus_success_shouldSaveAndPublish_andReturnResponse() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var oldUpdatedAt = LocalDateTime.now().minusDays(1);
        var app = appWith(applicationId, vacancyId, userId, 1L, oldUpdatedAt);

        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.just(app));
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());

        var oldStatus = status(1L, ApplicationStatusName.NEW);
        var newStatus = status(2L, ApplicationStatusName.REJECTED);

        when(applicationStatusRepositoryPort.findById(1L)).thenReturn(Mono.just(oldStatus));
        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.REJECTED)).thenReturn(Mono.just(newStatus));
        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Vacancy title"));

        when(applicationRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(applicationEventPublisherPort.publishStatusChanged(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.REJECTED)))
                .assertNext(resp -> {
                    assertEquals(ApplicationStatusName.REJECTED.getValue(), resp.status());
                    assertNotNull(resp.updateAt());
                })
                .verifyComplete();

        verify(applicationRepositoryPort).save(appCaptor.capture());
        var saved = appCaptor.getValue();

        assertEquals(2L, saved.getStatus());
        assertNotNull(saved.getUpdatedAt());
        assertTrue(saved.getUpdatedAt().isAfter(oldUpdatedAt));

        verify(applicationEventPublisherPort).publishStatusChanged(any());
    }

    @Test
    void updateApplicationStatus_publishError_shouldPropagate() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = appWith(applicationId, vacancyId, userId, 1L, LocalDateTime.now().minusDays(1));

        when(applicationRepositoryPort.findById(applicationId)).thenReturn(Mono.just(app));
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());

        when(applicationStatusRepositoryPort.findById(1L)).thenReturn(Mono.just(status(1L, ApplicationStatusName.NEW)));
        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.REJECTED)).thenReturn(Mono.just(status(2L, ApplicationStatusName.REJECTED)));
        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Vacancy title"));

        when(applicationRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        RuntimeException pubErr = new RuntimeException("kafka down");
        when(applicationEventPublisherPort.publishStatusChanged(any())).thenReturn(Mono.error(pubErr));

        StepVerifier.create(useCase.updateApplicationStatus(applicationId, new ApplicationStatusUpdateRequestDto(ApplicationStatusName.REJECTED)))
                .expectErrorSatisfies(e -> assertSame(pubErr, e))
                .verify();

        verify(applicationRepositoryPort).save(any());
        verify(applicationEventPublisherPort).publishStatusChanged(any());
    }


}