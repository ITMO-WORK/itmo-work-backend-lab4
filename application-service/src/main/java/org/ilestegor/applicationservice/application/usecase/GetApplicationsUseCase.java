package org.ilestegor.applicationservice.application.usecase;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.GetMyApplicationResponse;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.input.GetApplicationPort;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.mapper.ApplicationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetApplicationsUseCase implements GetApplicationPort {

    private final CurrentUserPort currentUserPort;
    private final ApplicationPreconditions preconditions;

    private final ApplicationRepositoryPort applicationRepositoryPort;
    private final VacancyPort vacancyPort;
    private final UserPort userPort;
    private final ApplicationMapper applicationMapper;
    private final ApplicationStatusRepositoryPort applicationStatusRepositoryPort;

    @Override
    public Mono<Page<GetMyApplicationResponse>> getApplicationByApplicationId(Pageable pageable) {
        return currentUserPort.getCurrentUser()
                .flatMap(user ->
                        preconditions.checkUserExists(user.userId(), user.token())
                                .then(Mono.defer(() -> fetchPage(user.userId(), pageable, user.token())))
                );
    }

    private Mono<Page<GetMyApplicationResponse>> fetchPage(UUID userId, Pageable pageable, String token) {

        Mono<List<GetMyApplicationResponse>> contentMono =
                applicationRepositoryPort.findAllByUserId(userId, pageable)
                        .flatMap(app ->
                                enrich(app, token)
                                        .onErrorResume(java.util.concurrent.TimeoutException.class,
                                                e -> Mono.just(applicationMapper.fromApplicationToGetMyApplicationResponse(app))
                                        )
                        )
                        .collectList();

        Mono<Long> totalMono = applicationRepositoryPort.countApplicationsByUserId(userId);

        return Mono.zip(contentMono, totalMono)
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    private Mono<GetMyApplicationResponse> enrich(Application app, String token) {

        Mono<String> vacancyTitleMono =
                vacancyPort.getVacancyTitle(app.getVacancyId(), token);

        Mono<String> userFullNameMono =
                userPort.checkUserExists(app.getUserId(), token)
                        .map(UserResponseDto::ownerFullName);

        Mono<String> applicationStatusMono =
                applicationStatusRepositoryPort.findById(app.getStatus())
                        .map(status -> status.getApplicationStatusName().getValue());

        return Mono.zip(vacancyTitleMono, userFullNameMono, applicationStatusMono)
                .map(t -> applicationMapper
                        .fromApplicationToGetMyApplicationResponse(app)
                        .toBuilder()
                        .vacancyTitle(t.getT1())
                        .userFullName(t.getT2())
                        .applicationStatus(t.getT3())
                        .build());
    }
}
