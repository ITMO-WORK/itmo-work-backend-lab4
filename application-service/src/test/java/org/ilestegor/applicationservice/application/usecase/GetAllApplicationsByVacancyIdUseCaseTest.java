package org.ilestegor.applicationservice.application.usecase;

import org.apache.kafka.common.errors.TimeoutException;
import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.mapper.ApplicationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllApplicationsByVacancyIdUseCaseTest {

    @Mock
    CurrentUserPort currentUserPort;
    @Mock
    ApplicationPreconditions applicationPreconditions;
    @Mock
    VacancyPort vacancyPort;
    @Mock
    ApplicationRepositoryPort applicationRepositoryPort;
    @Mock
    UserPort userPort;
    @Mock
    ApplicationMapper applicationMapper;

    @InjectMocks
    GetAllApplicationsByVacancyIdUseCase useCase;

    UUID userId;
    UUID vacancyId;
    String token;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        vacancyId = UUID.randomUUID();
        token = "jwt-token";
        pageable = PageRequest.of(0, 10);
    }

    private Application app(UUID id, UUID vacancyId, UUID userId) {
        return Application.builder()
                .id(id)
                .vacancyId(vacancyId)
                .userId(userId)
                .coverLetter("Hello")
                .status(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserResponseDto userDto(UUID userId, String fullName, String ownerEmail) {
        return new UserResponseDto(userId, fullName, ownerEmail);
    }

    private ApplicationDto baseDtoFromApp(Application a) {
        return ApplicationDto.builder()
                .id(a.getId())
                .vacancyId(a.getVacancyId())
                .userId(a.getUserId())
                .coverLetter(a.getCoverLetter())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    @Test
    void getAllApplicationsByVacancyId_userNotFound_shouldError() {
        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        RuntimeException err = new RuntimeException("user not found");
        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.error(err));

        when(applicationPreconditions.checkVacancyExists(any(), any())).thenReturn(Mono.empty());
        when(applicationPreconditions.checkUserBelongsToCompany(any(), any(), any())).thenReturn(Mono.empty());
        when(vacancyPort.getVacancyTitle(any(), any())).thenReturn(Mono.just("title"));


        StepVerifier.create(useCase.getAllApplicationsByVacancyId(vacancyId, pageable))
                .expectErrorSatisfies(e -> org.junit.jupiter.api.Assertions.assertSame(err, e))
                .verify();

        verify(applicationPreconditions).checkUserExists(userId, token);
        verifyNoInteractions(applicationMapper, userPort);
    }

    @Test
    void getAllApplicationsByVacancyId_vacancyNotFound_shouldError() {
        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        RuntimeException err = new RuntimeException("vacancy not found");
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.error(err));


        when(applicationPreconditions.checkUserBelongsToCompany(any(), any(), any())).thenReturn(Mono.empty());
        when(vacancyPort.getVacancyTitle(any(), any())).thenReturn(Mono.just("title"));


        StepVerifier.create(useCase.getAllApplicationsByVacancyId(vacancyId, pageable))
                .expectErrorSatisfies(e -> org.junit.jupiter.api.Assertions.assertSame(err, e))
                .verify();

        verify(applicationPreconditions).checkVacancyExists(vacancyId, token);
        verifyNoInteractions(applicationMapper, userPort);
    }

    @Test
    void getAllApplicationsByVacancyId_userNotBelongs_shouldError() {
        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());

        RuntimeException err = new RuntimeException("forbidden");
        when(applicationPreconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.error(err));


        when(vacancyPort.getVacancyTitle(any(), any())).thenReturn(Mono.just("title"));


        StepVerifier.create(useCase.getAllApplicationsByVacancyId(vacancyId, pageable))
                .expectErrorSatisfies(e -> org.junit.jupiter.api.Assertions.assertSame(err, e))
                .verify();

        verify(applicationPreconditions).checkUserBelongsToCompany(vacancyId, userId, token);
        verifyNoInteractions(applicationMapper, userPort);
    }


    @Test
    void getAllApplicationsByVacancyId_timeout_shouldReturnEmptyPage() {
        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());


        when(vacancyPort.getVacancyTitle(vacancyId, token))
                .thenReturn(Mono.error(new TimeoutException("timeout")));


        StepVerifier.create(useCase.getAllApplicationsByVacancyId(vacancyId, pageable))
                .assertNext(page -> {
                    org.junit.jupiter.api.Assertions.assertNotNull(page);
                    org.junit.jupiter.api.Assertions.assertEquals(0, page.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertTrue(page.getContent().isEmpty());
                    org.junit.jupiter.api.Assertions.assertEquals(pageable, page.getPageable());
                })
                .verifyComplete();
    }

    @Test
    void getAllApplicationsByVacancyId_success_emptyContent_shouldReturnEmptyPageWithTotal() {
        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());
        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Vacancy Title"));

        when(applicationRepositoryPort.findAllByVacancyId(vacancyId, pageable))
                .thenReturn(Flux.empty());
        when(applicationRepositoryPort.countApplicationByVacancyId(vacancyId))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(useCase.getAllApplicationsByVacancyId(vacancyId, pageable))
                .assertNext(page -> {
                    org.junit.jupiter.api.Assertions.assertEquals(5L, page.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();

        verifyNoInteractions(userPort, applicationMapper);
    }

    @Test
    void getAllApplicationsByVacancyId_success_twoItems_shouldEnrichAndReturnPage() {
        when(currentUserPort.getCurrentUser())
                .thenReturn(Mono.just(new CurrentUserPort.CurrentUser(userId, token)));

        when(applicationPreconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkVacancyExists(vacancyId, token)).thenReturn(Mono.empty());
        when(applicationPreconditions.checkUserBelongsToCompany(vacancyId, userId, token)).thenReturn(Mono.empty());
        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Vacancy Title"));

        var a1 = app(UUID.randomUUID(), vacancyId, UUID.randomUUID());
        var a2 = app(UUID.randomUUID(), vacancyId, UUID.randomUUID());

        when(applicationRepositoryPort.findAllByVacancyId(vacancyId, pageable))
                .thenReturn(Flux.just(a1, a2));
        when(applicationRepositoryPort.countApplicationByVacancyId(vacancyId))
                .thenReturn(Mono.just(2L));

        when(userPort.checkUserExists(eq(a1.getUserId()), eq(token)))
                .thenReturn(Mono.just(userDto(a1.getUserId(), "User One", "email@mail.ru")));
        when(userPort.checkUserExists(eq(a2.getUserId()), eq(token)))
                .thenReturn(Mono.just(userDto(a2.getUserId(), "User Two", "email2@mail.com")));

        when(applicationMapper.fromApplicationtoApplicationDto(a1)).thenReturn(baseDtoFromApp(a1));
        when(applicationMapper.fromApplicationtoApplicationDto(a2)).thenReturn(baseDtoFromApp(a2));

        StepVerifier.create(useCase.getAllApplicationsByVacancyId(vacancyId, pageable))
                .assertNext(page -> {
                    assertEquals(2L, page.getTotalElements());
                    assertEquals(2, page.getContent().size());

                    var dto1 = page.getContent().get(0);
                    var dto2 = page.getContent().get(1);

                    assertEquals("Vacancy Title", dto1.vacancyTitle());
                    assertEquals("Vacancy Title", dto2.vacancyTitle());

                    assertNotNull(dto1.userFullName());
                    assertNotNull(dto2.userFullName());
                })
                .verifyComplete();

        verify(applicationRepositoryPort).findAllByVacancyId(vacancyId, pageable);
        verify(applicationRepositoryPort).countApplicationByVacancyId(vacancyId);
        verify(userPort, times(2)).checkUserExists(any(), eq(token));
        verify(applicationMapper, times(2)).fromApplicationtoApplicationDto(any());
    }


}