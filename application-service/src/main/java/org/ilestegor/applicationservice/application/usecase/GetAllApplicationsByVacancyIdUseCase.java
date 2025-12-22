package org.ilestegor.applicationservice.application.usecase;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.TimeoutException;
import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.input.GetAllApplicationsByVacancyIdPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
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
public class GetAllApplicationsByVacancyIdUseCase implements GetAllApplicationsByVacancyIdPort {

    private final CurrentUserPort currentUserPort;
    private final ApplicationPreconditions applicationPreconditions;
    private final VacancyPort vacancyPort;
    private final ApplicationRepositoryPort applicationRepositoryPort;
    private final UserPort userPort;
    private final ApplicationMapper applicationMapper;

    @Override
    public Mono<Page<ApplicationDto>> getAllApplicationsByVacancyId(UUID vacancyId, Pageable pageable) {
        return currentUserPort.getCurrentUser()
                .flatMap(user -> applicationPreconditions.checkUserExists(user.userId(), user.token())
                        .then(applicationPreconditions.checkVacancyExists(vacancyId, user.token()))
                        .then(applicationPreconditions.checkUserBelongsToCompany(vacancyId, user.userId(), user.token()))
                        .then(vacancyPort.getVacancyTitle(vacancyId, user.token()))
                        .flatMap(vacancyTitle -> fetchPage(vacancyId, pageable, vacancyTitle, user.token())).onErrorResume(TimeoutException.class, e ->
                                Mono.just(new PageImpl<>(List.of(), pageable, 0))
                        )
                );
    }

    private Mono<Page<ApplicationDto>> fetchPage(UUID vacancyId, Pageable pageable, String vacancyTitle, String token) {
        Mono<List<ApplicationDto>> contentMono =
                applicationRepositoryPort.findAllByVacancyId(vacancyId, pageable)
                        .flatMap(app ->
                                userPort.checkUserExists(app.getUserId(), token)
                                        .map(u -> {
                                            var dto = applicationMapper.fromApplicationtoApplicationDto(app);
                                            return dto.toBuilder()
                                                    .userFullName(u.ownerFullName())
                                                    .vacancyTitle(vacancyTitle)
                                                    .build();
                                        })
                        )
                        .collectList();

        Mono<Long> totalMono = applicationRepositoryPort.countApplicationByVacancyId(vacancyId);

        return Mono.zip(contentMono, totalMono)
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }
}
