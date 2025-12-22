package org.ilestegor.applicationservice.application.common;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CompanyPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.exception.exceptions.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ApplicationPreconditions {

    private final UserPort userPort;

    private final VacancyPort vacancyPort;

    private final CompanyPort companyPort;

    private final ApplicationRepositoryPort applicationRepositoryPort;

    public Mono<Void> checkUserHasNotApplied(UUID userId, UUID vacancyId) {
        return applicationRepositoryPort.existsByUserIdAndVacancyId(userId, vacancyId).flatMap(
                exists -> {
                    if (exists) return Mono.error(new UserHasAlreadyAppliedException());
                    return Mono.empty();
                }
        );
    }

    public Mono<UserResponseDto> checkUserExists(UUID userId, String token) {
        return userPort.checkUserExists(userId, token)
                .flatMap(dto -> (dto == null || dto.userId() == null)
                        ? Mono.error(new UserNotFoundException())
                        : Mono.just(dto));
    }

    public Mono<Void> checkVacancyExists(UUID vacancyId, String token) {
        return vacancyPort.checkVacancyExists(vacancyId, token)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new VacancyNotFoundException()));
    }

    public Mono<Void> checkVacancyIsPublished(UUID vacancyId, String token) {
        return vacancyPort.checkVacancyIsPublished(vacancyId, token)
                .flatMap(published -> published
                        ? Mono.empty()
                        : Mono.error(new VacancyNotPublishedException()));
    }


    public Mono<Application> checkUserOwnsApplication(UUID applicationId, UUID userId) {
        return applicationRepositoryPort.findById(applicationId)
                .switchIfEmpty(Mono.error(new ApplicationNotFoundException()))
                .flatMap(app -> {
                    if (userId.equals(app.getUserId())) return Mono.just(app);
                    return Mono.error(new ApplicationNotFoundException());
                });
    }

    public Mono<Void> checkUserBelongsToCompany(UUID vacancyId, UUID userId, String token) {
        return vacancyPort.getCompanyIdByVacancyId(vacancyId, token)
                .flatMap(companyId -> companyPort.isUserBelongsToCompany(companyId, userId, token))
                .flatMap(belongs -> {
                    if (belongs) return Mono.empty();
                    return Mono.error(new UserDoesNotBelongsToCompanyException());
                });
    }

    public Mono<Application> checkUserOwnsApplicationByUserId(UUID userId) {
        return applicationRepositoryPort.findApplicationIdByUserId(userId)
                .switchIfEmpty(Mono.error(new ApplicationNotFoundException()))
                .flatMap(app -> {
                    if (userId.equals(app.getUserId())) {
                        return Mono.just(app);
                    }
                    return Mono.error(new ApplicationNotFoundException());
                });
    }
}
