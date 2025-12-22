package org.ilestegor.applicationservice.application.usecase;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.input.GetApplicationPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.ilestegor.applicationservice.mapper.ApplicationMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class GetApplicationUseCase implements GetApplicationPort {

    private final CurrentUserPort currentUserPort;
    private final ApplicationPreconditions preconditions;
    private final VacancyPort vacancyPort;
    private final UserPort userPort;
    private final ApplicationMapper applicationMapper;

    @Override
    public Mono<ApplicationDto> getApplicationByApplicationId() {
        return currentUserPort.getCurrentUser()
                .flatMap(user ->
                        preconditions.checkUserExists(user.userId(), user.token())
                                .then(preconditions.checkUserOwnsApplicationByUserId(user.userId()))
                                .flatMap(app -> enrichSingle(app, user.token())
                                        .onErrorResume(TimeoutException.class, e ->
                                                Mono.just(applicationMapper.fromApplicationtoApplicationDto(app))
                                        )
                                )
                );
    }

    private Mono<ApplicationDto> enrichSingle(org.ilestegor.applicationservice.domain.Application app, String token) {
        Mono<String> vacancyTitleMono = vacancyPort.getVacancyTitle(app.getVacancyId(), token);
        Mono<String> userFullNameMono = userPort.checkUserExists(app.getUserId(), token)
                .map(UserResponseDto::ownerFullName);

        return Mono.zip(vacancyTitleMono, userFullNameMono)
                .map(t -> {
                    var dto = applicationMapper.fromApplicationtoApplicationDto(app);
                    return dto.toBuilder()
                            .vacancyTitle(t.getT1())
                            .userFullName(t.getT2())
                            .build();
                });
    }
}
