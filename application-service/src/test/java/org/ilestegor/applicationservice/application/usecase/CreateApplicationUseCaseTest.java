package org.ilestegor.applicationservice.application.usecase;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationCreateEventDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.output.ApplicationEventPublisherPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationStatusRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.exception.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateApplicationUseCaseTest {

    @Mock
    private CurrentUserPort currentUserPort;
    @Mock
    private ApplicationPreconditions preconditions;
    @Mock
    private ApplicationStatusRepositoryPort applicationStatusRepositoryPort;
    @Mock
    private ApplicationRepositoryPort applicationRepositoryPort;
    @Mock
    private ApplicationEventPublisherPort applicationEventPublisherPort;

    @InjectMocks
    private CreateApplicationUseCase useCase;

    private UUID vacancyId;
    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        vacancyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        token = "jwt-token";
    }

    private ApplicationStatus newStatus() {
        var st = new ApplicationStatus();
        st.setId(10L);
        st.setApplicationStatusName(ApplicationStatusName.NEW);
        return st;
    }

    private Application savedApp() {
        LocalDateTime now = LocalDateTime.now();
        return Application.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .vacancyId(vacancyId)
                .coverLetter("Hello")
                .status(10L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }


    @Test
    void createApplication_ok_shouldSavePublishAndReturnResponse() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.just(mockUserDto(userId, "User", "emaul@mail.com")));
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserHasNotApplied(userId, vacancyId)).thenReturn(Mono.empty());

        var status = mock(ApplicationStatus.class);
        when(status.getId()).thenReturn(1L);
        when(status.getApplicationStatusName()).thenReturn(ApplicationStatusName.NEW);
        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.NEW))
                .thenReturn(Mono.just(status));

        UUID appId = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now().minusMinutes(1);
        LocalDateTime updated = LocalDateTime.now().minusMinutes(1);

        var saved = Application.builder()
                .id(appId)
                .userId(userId)
                .vacancyId(vacancyId)
                .coverLetter(dto.coverLetter())
                .status(status.getId())
                .createdAt(created)
                .updatedAt(updated)
                .build();

        ArgumentCaptor<Application> appCaptor = ArgumentCaptor.forClass(Application.class);
        when(applicationRepositoryPort.save(appCaptor.capture()))
                .thenReturn(Mono.just(saved));

        when(applicationEventPublisherPort.publishApplicationCreate(any(ApplicationCreateEventDto.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .assertNext(resp -> {
                    assertEquals(appId, resp.id());
                    assertEquals(ApplicationStatusName.NEW.getValue(), resp.status());
                    assertEquals(created, resp.createdAt());
                    assertEquals(updated, resp.updatedAt());
                    assertEquals(dto.coverLetter(), resp.coverLetter());
                })
                .verifyComplete();


        verify(preconditions).checkUserExists(userId, token);
        verify(preconditions).checkVacancyExists(vacancyId, token);
        verify(preconditions).checkVacancyIsPublished(vacancyId, token);
        verify(preconditions).checkUserHasNotApplied(userId, vacancyId);

        verify(applicationStatusRepositoryPort).findByName(ApplicationStatusName.NEW);
        verify(applicationRepositoryPort).save(any(Application.class));
        verify(applicationEventPublisherPort).publishApplicationCreate(any(ApplicationCreateEventDto.class));

        Application toSave = appCaptor.getValue();
        assertEquals(userId, toSave.getUserId());
        assertEquals(vacancyId, toSave.getVacancyId());
        assertEquals(dto.coverLetter(), toSave.getCoverLetter());
        assertEquals(1L, toSave.getStatus());

        assertNotNull(toSave.getCreatedAt());
        assertNotNull(toSave.getUpdatedAt());
    }

    @Test
    void createApplication_userNotFound_shouldError_andNoSideEffects() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        var err = new UserNotFoundException("user not found");
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.error(err));


        when(preconditions.checkVacancyExists(any(), any())).thenReturn(Mono.empty());
        when(preconditions.checkVacancyIsPublished(any(), any())).thenReturn(Mono.empty());
        when(preconditions.checkUserHasNotApplied(any(), any())).thenReturn(Mono.empty());
        when(applicationStatusRepositoryPort.findByName(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectErrorSatisfies(e -> org.junit.jupiter.api.Assertions.assertSame(err, e))
                .verify();

        verify(preconditions).checkUserExists(userId, token);
        verifyNoInteractions(applicationRepositoryPort, applicationEventPublisherPort);
    }

    @Test
    void createApplication_vacancyNotFound_shouldError_andNoSideEffects() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var err = new VacancyNotFoundException();
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.error(err));


        when(preconditions.checkVacancyIsPublished(any(), any())).thenReturn(Mono.empty());
        when(preconditions.checkUserHasNotApplied(any(), any())).thenReturn(Mono.empty());
        when(applicationStatusRepositoryPort.findByName(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectError(VacancyNotFoundException.class)
                .verify();

        verify(preconditions).checkUserExists(userId, token);
        verify(preconditions).checkVacancyExists(vacancyId, token);
        verifyNoInteractions(applicationRepositoryPort, applicationEventPublisherPort);
    }

    @Test
    void createApplication_vacancyNotPublished_shouldError_andNoSideEffects() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());

        var err = new VacancyNotPublishedException();
        when(preconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.error(err));


        when(preconditions.checkUserHasNotApplied(any(), any())).thenReturn(Mono.empty());
        when(applicationStatusRepositoryPort.findByName(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectError(VacancyNotPublishedException.class)
                .verify();

        verify(preconditions).checkVacancyIsPublished(vacancyId, token);
        verifyNoInteractions(applicationRepositoryPort, applicationEventPublisherPort);
    }

    @Test
    void createApplication_userAlreadyApplied_shouldError_andNoSideEffects() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());

        var err = new UserHasAlreadyAppliedException();
        when(preconditions.checkUserHasNotApplied(userId, vacancyId)).thenReturn(Mono.error(err));


        when(applicationStatusRepositoryPort.findByName(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectError(UserHasAlreadyAppliedException.class)
                .verify();

        verify(preconditions).checkUserHasNotApplied(userId, vacancyId);
        verifyNoInteractions(applicationRepositoryPort, applicationEventPublisherPort);
    }

    @Test
    void createApplication_statusNotFound_shouldError_andNoSideEffects() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserHasNotApplied(userId, vacancyId)).thenReturn(Mono.empty());

        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.NEW))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectError(ApplicationStatusNotFoundException.class)
                .verify();

        verify(applicationStatusRepositoryPort).findByName(ApplicationStatusName.NEW);
        verifyNoInteractions(applicationRepositoryPort, applicationEventPublisherPort);
    }

    @Test
    void createApplication_saveFails_shouldPropagate_andNotPublish() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserHasNotApplied(userId, vacancyId)).thenReturn(Mono.empty());

        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.NEW))
                .thenReturn(Mono.just(newStatus()));

        var err = new RuntimeException("db down");
        when(applicationRepositoryPort.save(any(Application.class)))
                .thenReturn(Mono.error(err));

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectErrorSatisfies(e -> org.junit.jupiter.api.Assertions.assertSame(err, e))
                .verify();

        verify(applicationRepositoryPort).save(any());
        verifyNoInteractions(applicationEventPublisherPort);
    }

    @Test
    void createApplication_publishFails_shouldPropagate() {
        var dto = new ApplicationCreateRequestDto("Hello");

        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkVacancyIsPublished(vacancyId, token)).thenReturn(Mono.empty());
        when(preconditions.checkUserHasNotApplied(userId, vacancyId)).thenReturn(Mono.empty());

        when(applicationStatusRepositoryPort.findByName(ApplicationStatusName.NEW))
                .thenReturn(Mono.just(newStatus()));

        when(applicationRepositoryPort.save(any(Application.class)))
                .thenReturn(Mono.just(savedApp()));

        var err = new RuntimeException("kafka down");
        when(applicationEventPublisherPort.publishApplicationCreate(any()))
                .thenReturn(Mono.error(err));

        StepVerifier.create(useCase.createApplication(vacancyId, dto))
                .expectErrorSatisfies(e -> org.junit.jupiter.api.Assertions.assertSame(err, e))
                .verify();

        verify(applicationEventPublisherPort).publishApplicationCreate(any());
    }


    private UserResponseDto mockUserDto(UUID userId, String userName, String email) {
        return new UserResponseDto(userId, userName, email);
    }
}