package org.ilestegor.applicationservice.application.usecase;

import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationStatusRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
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
class UpdateApplicationUseCaseTest {
    @Captor
    ArgumentCaptor<Application> appCaptor;
    @Mock
    private CurrentUserPort currentUserPort;
    @Mock
    private ApplicationPreconditions applicationPreconditions;
    @Mock
    private ApplicationRepositoryPort applicationRepositoryPort;
    @Mock
    private ApplicationStatusRepositoryPort applicationStatusRepositoryPort;
    @InjectMocks
    private UpdateApplicationUseCase useCase;
    private UUID userId;
    private UUID appId;
    private UUID vacancyId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        appId = UUID.randomUUID();
        vacancyId = UUID.randomUUID();
        token = "jwt-token";
    }


    private Application app(UUID id, UUID vacancyId, UUID userId, long statusId) {
        return Application.builder()
                .id(id)
                .vacancyId(vacancyId)
                .userId(userId)
                .status(statusId)
                .coverLetter("old")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private ApplicationStatus status(long id, ApplicationStatusName name) {
        var st = new ApplicationStatus();
        st.setId(id);
        st.setApplicationStatusName(name);
        return st;
    }

    @Test
    void updateApplication_checkUserExistsError_shouldPropagate() {
        var dto = new ApplicationCreateRequestDto("new cover");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        RuntimeException err = new RuntimeException("user not found");
        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.error(err));


        when(applicationPreconditions.checkUserOwnsApplication(appId, userId))
                .thenReturn(Mono.just(app(appId, vacancyId, userId, 1L)));


        StepVerifier.create(useCase.updateApplication(appId, dto))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();

        verify(currentUserPort).getCurrentUser();
        verify(applicationPreconditions).checkUserExists(userId, token);

        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    void updateApplication_checkUserOwnsApplicationError_shouldPropagate() {
        var dto = new ApplicationCreateRequestDto("new cover");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        RuntimeException err = new RuntimeException("no access");
        when(applicationPreconditions.checkUserOwnsApplication(appId, userId)).thenReturn(Mono.error(err));


        StepVerifier.create(useCase.updateApplication(appId, dto))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();

        verify(applicationPreconditions).checkUserExists(userId, token);
        verify(applicationPreconditions).checkUserOwnsApplication(appId, userId);

        verify(applicationPreconditions, never()).checkVacancyExists(any(), any());
        verify(applicationPreconditions, never()).checkVacancyIsPublished(any(), any());
        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    void updateApplication_checkVacancyExistsError_shouldPropagate_andNotSave() {
        var dto = new ApplicationCreateRequestDto("new cover");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var existing = app(appId, vacancyId, userId, 1L);
        when(applicationPreconditions.checkUserOwnsApplication(appId, userId)).thenReturn(Mono.just(existing));

        RuntimeException err = new RuntimeException("vacancy not found");
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.error(err));


        when(applicationPreconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());


        StepVerifier.create(useCase.updateApplication(appId, dto))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();

        verify(applicationPreconditions).checkVacancyExists(vacancyId, token);
        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    void updateApplication_checkVacancyIsPublishedError_shouldPropagate_andNotSave() {
        var dto = new ApplicationCreateRequestDto("new cover");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var existing = app(appId, vacancyId, userId, 1L);
        when(applicationPreconditions.checkUserOwnsApplication(appId, userId)).thenReturn(Mono.just(existing));

        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());

        RuntimeException err = new RuntimeException("not published");
        when(applicationPreconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.error(err));


        StepVerifier.create(useCase.updateApplication(appId, dto))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();

        verify(applicationPreconditions).checkVacancyExists(vacancyId, token);
        verify(applicationPreconditions).checkVacancyIsPublished(vacancyId, token);
        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    void updateApplication_success_shouldUpdateCoverLetterUpdatedAt_saveAndReturnDto() {
        var dto = new ApplicationCreateRequestDto("new cover");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var existing = app(appId, vacancyId, userId, 1L);
        var oldUpdatedAt = existing.getUpdatedAt();

        when(applicationPreconditions.checkUserOwnsApplication(appId, userId)).thenReturn(Mono.just(existing));
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());


        when(applicationRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        when(applicationStatusRepositoryPort.findById(1L))
                .thenReturn(Mono.just(status(1L, ApplicationStatusName.NEW)));

        StepVerifier.create(useCase.updateApplication(appId, dto))
                .assertNext(resp -> {
                    assertNotNull(resp);
                    assertEquals(appId, resp.id());
                    assertEquals(ApplicationStatusName.NEW.getValue(), resp.status());
                    assertEquals("new cover", resp.coverLetter());
                    assertNotNull(resp.updatedAt());
                })
                .verifyComplete();

        verify(applicationRepositoryPort).save(appCaptor.capture());
        var saved = appCaptor.getValue();
        assertEquals("new cover", saved.getCoverLetter());
        assertNotNull(saved.getUpdatedAt());
        assertTrue(saved.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    void updateApplication_statusNotFound_shouldError() {
        var dto = new ApplicationCreateRequestDto("new cover");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var existing = app(appId, vacancyId, userId, 999L);

        when(applicationPreconditions.checkUserOwnsApplication(appId, userId)).thenReturn(Mono.just(existing));
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());

        when(applicationRepositoryPort.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        when(applicationStatusRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateApplication(appId, dto))
                .expectError(ApplicationStatusNotFoundException.class)
                .verify();

        verify(applicationRepositoryPort).save(any());
        verify(applicationStatusRepositoryPort).findById(999L);
    }
}