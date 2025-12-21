package org.ilestegor.applicationservice.application.usecase;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationCreateResponseDto;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationCreateEventDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.input.CreateApplicationPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationEventPublisherPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;

import org.ilestegor.applicationservice.application.port.output.ApplicationStatusRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationStatusNotFoundException;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateApplicationUseCase implements CreateApplicationPort {

    private final CurrentUserPort currentUserPort;
    private final ApplicationPreconditions preconditions;

    private final ApplicationStatusRepositoryPort applicationStatusRepositoryPort;
    private final ApplicationRepositoryPort applicationRepositoryPort;
    private final ApplicationEventPublisherPort applicationEventPublisherPort;

    @Override
    public Mono<ApplicationCreateResponseDto> createApplication(
            UUID vacancyId,
            ApplicationCreateRequestDto dto,
            FilePart resume,
            UUID replacedField
    ) {
        return currentUserPort.getCurrentUser()
                .flatMap(user ->
                        preconditions.checkUserExists(user.userId(), user.token())
                                .then(preconditions.checkVacancyExists(vacancyId, user.token()))
                                .then(preconditions.checkVacancyIsPublished(vacancyId, user.token()))
                                .then(preconditions.checkUserHasNotApplied(user.userId(), vacancyId))
                                .then(applicationStatusRepositoryPort
                                        .findByName(ApplicationStatusName.NEW)
                                        .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()))
                                )
                                .flatMap(status -> {
                                    Application app = createNewApplication(
                                            user.userId(),
                                            vacancyId,
                                            dto,
                                            status.getId()
                                    );

                                    return  applicationRepositoryPort.save(app)
                                            .flatMap(saved -> {
                                                var event = new ApplicationCreateEventDto(
                                                        saved.getId(),
                                                        saved.getUserId(),
                                                        OffsetDateTime.now()
                                                );
                                                return applicationEventPublisherPort.publishApplicationCreate(event)
                                                        .thenReturn(new ApplicationCreateResponseDto(
                                                                saved.getId(),
                                                                status.getApplicationStatusName().getValue(),
                                                                saved.getCreatedAt(),
                                                                saved.getUpdatedAt(),
                                                                saved.getCoverLetter()
                                                        ));
                                            });
                                })
                );
    }

    private Application createNewApplication(
            UUID userId,
            UUID vacancyId,
            ApplicationCreateRequestDto dto,
            Long statusId
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Application.builder()
                .userId(userId)
                .vacancyId(vacancyId)
                .coverLetter(dto.coverLetter())
                .status(statusId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}