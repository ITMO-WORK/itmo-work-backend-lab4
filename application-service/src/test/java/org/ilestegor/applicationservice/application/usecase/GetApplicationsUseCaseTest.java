package org.ilestegor.applicationservice.application.usecase;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.GetMyApplicationResponse;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.mapper.ApplicationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GetApplicationsUseCaseTest {
    UUID userId;
    UUID vacancyId;
    UUID fileId;
    UUID appId1;
    UUID appId2;
    String token;
    Pageable pageable;
    @Captor
    ArgumentCaptor<Application> applicationCaptor;
    @Mock
    private CurrentUserPort currentUserPort;
    @Mock
    private ApplicationPreconditions preconditions;
    @Mock
    private ApplicationRepositoryPort applicationRepositoryPort;
    @Mock
    private VacancyPort vacancyPort;
    @Mock
    private UserPort userPort;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private ApplicationStatusRepositoryPort applicationStatusRepositoryPort;
    @InjectMocks
    private GetApplicationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetApplicationsUseCase(
                currentUserPort,
                preconditions,
                applicationRepositoryPort,
                vacancyPort,
                userPort,
                applicationMapper,
                applicationStatusRepositoryPort
        );

        userId = UUID.randomUUID();
        vacancyId = UUID.randomUUID();
        fileId = UUID.randomUUID();
        appId1 = UUID.randomUUID();
        appId2 = UUID.randomUUID();
        token = "jwt-token";
        pageable = PageRequest.of(0, 10);
    }


    private Application app(UUID vacancyId, UUID applicantId, long statusId) {
        return Application.builder()
                .id(UUID.randomUUID())
                .userId(applicantId)
                .vacancyId(vacancyId)
                .status(statusId)
                .coverLetter("cover")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .fileId(UUID.randomUUID())
                .build();
    }

    private GetMyApplicationResponse baseResponseFromMapper(Application a) {
        return GetMyApplicationResponse.builder()
                .id(a.getId())
                .coverLetter(a.getCoverLetter())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .fileId(a.getFileId())
                .vacancyTitle(null)
                .userFullName(null)
                .applicationStatus(null)
                .build();
    }

    private UserResponseDto userDto(String fullName, String email) {

        return new UserResponseDto(userId, fullName, email);
    }

    private ApplicationStatus status(long id, ApplicationStatusName name) {
        var st = new ApplicationStatus();
        st.setId(id);
        st.setApplicationStatusName(name);
        return st;
    }


    @Test
    void getApplicationByApplicationId_checkUserExistsError_shouldPropagate_andNotCallRepository() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));

        RuntimeException err = new RuntimeException("user not found");
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.error(err));

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();

        verify(currentUserPort).getCurrentUser();
        verify(preconditions).checkUserExists(userId, token);

        verifyNoInteractions(applicationRepositoryPort, vacancyPort, userPort, applicationMapper, applicationStatusRepositoryPort);
    }

    @Test
    void getApplicationByApplicationId_empty_shouldReturnEmptyPage() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.empty());
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .assertNext(page -> {
                    assertNotNull(page);
                    assertEquals(0, page.getTotalElements());
                    assertTrue(page.getContent().isEmpty());
                    assertEquals(pageable, page.getPageable());
                })
                .verifyComplete();

        verify(applicationRepositoryPort).findAllByUserId(userId, pageable);
        verify(applicationRepositoryPort).countApplicationsByUserId(userId);

        verifyNoInteractions(vacancyPort, userPort, applicationMapper, applicationStatusRepositoryPort);
    }


    @Test
    void getApplicationByApplicationId_success_singleItem_shouldEnrichAllFields() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        UUID appOwnerId = UUID.randomUUID();
        var app = app(vacancyId, appId1, 1L);
        app.setUserId(appOwnerId);

        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.just(app));
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.just(1L));

        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Vacancy title"));
        var u = mock(UserResponseDto.class);
        when(u.ownerFullName()).thenReturn("John Doe");
        when(userPort.checkUserExists(appOwnerId, token)).thenReturn(Mono.just(u));

        when(applicationStatusRepositoryPort.findById(1L)).thenReturn(Mono.just(status(1, ApplicationStatusName.NEW)));

        var base = baseResponseFromMapper(app);
        when(applicationMapper.fromApplicationToGetMyApplicationResponse(app)).thenReturn(base);

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    assertEquals(1, page.getContent().size());

                    var dto = page.getContent().getFirst();
                    assertEquals(app.getId(), dto.id());
                    assertEquals("Vacancy title", dto.vacancyTitle());
                    assertEquals("John Doe", dto.userFullName());
                    assertEquals(ApplicationStatusName.NEW.getValue(), dto.applicationStatus());
                })
                .verifyComplete();

        verify(vacancyPort).getVacancyTitle(vacancyId, token);
        verify(userPort).checkUserExists(appOwnerId, token);
        verify(applicationStatusRepositoryPort).findById(1L);
        verify(applicationMapper).fromApplicationToGetMyApplicationResponse(app);
    }

    @Test
    void getApplicationByApplicationId_success_multipleItems_shouldReturnEnrichedList() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());


        var app1 = app(vacancyId, appId1, 1L);
        var app2 = app(vacancyId, appId2, 2L);

        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.just(app1, app2));
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.just(2L));

        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Title"));

        var u1 = mock(UserResponseDto.class);
        when(u1.ownerFullName()).thenReturn("User1");
        when(userPort.checkUserExists(appId1, token)).thenReturn(Mono.just(u1));

        var u2 = mock(UserResponseDto.class);
        when(u2.ownerFullName()).thenReturn("User2");
        when(userPort.checkUserExists(appId2, token)).thenReturn(Mono.just(u2));

        when(applicationStatusRepositoryPort.findById(1L))
                .thenReturn(Mono.just(status(1L, ApplicationStatusName.NEW)));
        when(applicationStatusRepositoryPort.findById(2L))
                .thenReturn(Mono.just(status(2L, ApplicationStatusName.REJECTED)));

        when(applicationMapper.fromApplicationToGetMyApplicationResponse(app1))
                .thenReturn(baseResponseFromMapper(app1));
        when(applicationMapper.fromApplicationToGetMyApplicationResponse(app2))
                .thenReturn(baseResponseFromMapper(app2));

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements());
                    assertEquals(2, page.getContent().size());


                    var ids = page.getContent().stream().map(GetMyApplicationResponse::id).toList();
                    assertTrue(ids.containsAll(List.of(app1.getId(), app2.getId())));

                    var dto1 = page.getContent().get(0);
                    assertEquals("Title", dto1.vacancyTitle());
                    assertNotNull(dto1.userFullName());
                    assertNotNull(dto1.applicationStatus());
                })
                .verifyComplete();

        verify(applicationMapper).fromApplicationToGetMyApplicationResponse(app1);
        verify(applicationMapper).fromApplicationToGetMyApplicationResponse(app2);
        verify(userPort).checkUserExists(appId1, token);
        verify(userPort).checkUserExists(appId2, token);
    }

    @Test
    void getApplicationByApplicationId_enrichTimeout_shouldFallbackToBaseMapperResponse() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = app(vacancyId, appId1, 1L);

        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.just(app));
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.just(1L));


        when(vacancyPort.getVacancyTitle(vacancyId, token))
                .thenReturn(Mono.error(new java.util.concurrent.TimeoutException("timeout")));

        var u = mock(UserResponseDto.class);
        when(userPort.checkUserExists(app.getUserId(), token)).thenReturn(Mono.just(u));

        when(applicationStatusRepositoryPort.findById(app.getStatus()))
                .thenReturn(Mono.just(status(app.getStatus(), ApplicationStatusName.NEW)));

        var base = baseResponseFromMapper(app);
        when(applicationMapper.fromApplicationToGetMyApplicationResponse(app)).thenReturn(base);

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    var dto = page.getContent().get(0);
                    assertSame(base, dto);
                })
                .verifyComplete();

        verify(vacancyPort).getVacancyTitle(vacancyId, token);
        verify(userPort).checkUserExists(app.getUserId(), token);
        verify(applicationStatusRepositoryPort).findById(app.getStatus());
        verify(applicationMapper).fromApplicationToGetMyApplicationResponse(app);
    }

    @Test
    void getApplicationByApplicationId_repositoryFindAllError_shouldPropagate() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        RuntimeException err = new RuntimeException("db down");
        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.error(err));
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();
    }

    @Test
    void getApplicationByApplicationId_repositoryCountError_shouldPropagate() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.empty());

        RuntimeException err = new RuntimeException("count failed");
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.error(err));

        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();
    }

    @Test
    void getApplicationByApplicationId_enrichNonTimeoutError_shouldPropagate() {
        var currentUser = new CurrentUserPort.CurrentUser(userId, token);
        when(currentUserPort.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(preconditions.checkUserExists(userId, token)).thenReturn(Mono.empty());

        var app = app(vacancyId, appId1, 1L);

        when(applicationRepositoryPort.findAllByUserId(userId, pageable)).thenReturn(Flux.just(app));
        when(applicationRepositoryPort.countApplicationsByUserId(userId)).thenReturn(Mono.just(1L));

        when(vacancyPort.getVacancyTitle(vacancyId, token)).thenReturn(Mono.just("Title"));
        var u = mock(UserResponseDto.class);
        when(u.ownerFullName()).thenReturn("FN");
        when(userPort.checkUserExists(app.getUserId(), token)).thenReturn(Mono.just(u));

        RuntimeException err = new RuntimeException("status repo error");
        when(applicationStatusRepositoryPort.findById(1L)).thenReturn(Mono.error(err));


        StepVerifier.create(useCase.getApplicationByApplicationId(pageable))
                .expectErrorSatisfies(e -> assertSame(err, e))
                .verify();
    }
}