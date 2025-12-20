package org.ilestegor.applicationservice.application.usecase;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationStatusUpdateResponseDto;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationStatusChangeEvent;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.input.UpdateApplicationStatusPort;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationNotFoundException;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationStatusNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UpdateApplicationStatusUseCase implements UpdateApplicationStatusPort {

    private final CurrentUserPort currentUserPort;
    private final ApplicationPreconditions preconditions;

    private final ApplicationRepositoryPort applicationRepositoryPort;
    private final ApplicationStatusRepositoryPort applicationStatusRepositoryPort;
    private final ApplicationEventPublisherPort applicationEventPublisherPort;

    private final VacancyPort vacancyPort;

    @Override
    public Mono<ApplicationStatusUpdateResponseDto> updateApplicationStatus(
            UUID applicationId,
            ApplicationStatusUpdateRequestDto req
    ) {
        return currentUserPort.getCurrentUser()
                .flatMap(user -> preconditions.checkUserExists(user.userId(), user.token())
                        .then(loadApplicationOrFail(applicationId))
                        .flatMap(app -> loadVacancyIdOrFail(app)
                                .flatMap(vacancyId -> runAuthorizationChecks(user, vacancyId)
                                        .then(updateStatusAndPublish(user, app, vacancyId, req))
                                )
                        )
                );
    }

    private Mono<Application> loadApplicationOrFail(UUID applicationId) {
        return applicationRepositoryPort.findById(applicationId)
                .switchIfEmpty(Mono.error(new ApplicationNotFoundException()));
    }

    private Mono<UUID> loadVacancyIdOrFail(Application app) {
        if (app.getVacancyId() != null) {
            return Mono.just(app.getVacancyId());
        }
        return applicationRepositoryPort.findVacancyIdById(app.getId())
                .switchIfEmpty(Mono.error(new ApplicationNotFoundException()));
    }

    private Mono<Void> runAuthorizationChecks(CurrentUserPort.CurrentUser user, UUID vacancyId) {
        return preconditions.checkVacancyExists(vacancyId, user.token())
                .then(preconditions.checkUserBelongsToCompany(vacancyId, user.userId(), user.token()));
    }

    private Mono<ApplicationStatusUpdateResponseDto> updateStatusAndPublish(
            CurrentUserPort.CurrentUser user,
            Application app,
            UUID vacancyId,
            ApplicationStatusUpdateRequestDto req
    ) {
        Long oldStatusId = app.getStatus();

        return Mono.zip(
                        loadStatusById(oldStatusId),
                        loadStatusByName(req.applicationStatusName()),
                        vacancyPort.getVacancyTitle(vacancyId, user.token())
                )
                .flatMap(tuple -> {
                    var oldStatus = tuple.getT1();
                    var newStatus = tuple.getT2();
                    var title = tuple.getT3();

                    applyNewStatus(app, newStatus.getId());

                    return applicationRepositoryPort.save(app)
                            .flatMap(saved -> publishStatusChangedEvent(saved, vacancyId, title, oldStatus, newStatus))
                            .thenReturn(new ApplicationStatusUpdateResponseDto(
                                    newStatus.getApplicationStatusName().getValue(),
                                    app.getUpdatedAt()
                            ));
                });
    }

    private Mono<ApplicationStatus> loadStatusById(Long id) {
        return applicationStatusRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()));
    }

    private Mono<ApplicationStatus> loadStatusByName(ApplicationStatusName name) {
        return applicationStatusRepositoryPort.findByName(name)
                .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()));
    }

    private void applyNewStatus(Application app, Long newStatusId) {
        app.setStatus(newStatusId);
        app.setUpdatedAt(LocalDateTime.now());
    }

    private Mono<Void> publishStatusChangedEvent(
            Application saved,
            UUID vacancyId,
            String title,
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus
    ) {
        var event = new ApplicationStatusChangeEvent(
                saved.getId(),
                vacancyId,
                saved.getUserId(),
                title,
                oldStatus.getApplicationStatusName().getValue(),
                newStatus.getApplicationStatusName().getValue()
        );

        return applicationEventPublisherPort.publishStatusChanged(event);
    }
}
