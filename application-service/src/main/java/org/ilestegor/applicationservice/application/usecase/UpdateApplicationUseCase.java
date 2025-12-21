package org.ilestegor.applicationservice.application.usecase;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationCreateResponseDto;
import org.ilestegor.applicationservice.application.common.ApplicationPreconditions;
import org.ilestegor.applicationservice.application.port.input.UpdateApplicationPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationStatusRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationStatusNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateApplicationUseCase implements UpdateApplicationPort {

    private final CurrentUserPort currentUserPort;
    private final ApplicationPreconditions applicationPreconditions;
    private final ApplicationRepositoryPort applicationRepositoryPort;
    private final ApplicationStatusRepositoryPort applicationStatusRepositoryPort;

    @Override
    public Mono<ApplicationCreateResponseDto> updateApplication(UUID applicationId,
                                                                ApplicationCreateRequestDto dto) {

        return currentUserPort.getCurrentUser()
                .flatMap(user ->
                        applicationPreconditions.checkUserExists(user.userId(), user.token())
                                .then(applicationPreconditions.checkUserOwnsApplication(applicationId, user.userId()))
                                .flatMap(existing ->
                                        applicationPreconditions.checkVacancyExists(existing.getVacancyId(), user.token())
                                                .then(applicationPreconditions.checkVacancyIsPublished(existing.getVacancyId(), user.token()))
                                                .thenReturn(existing)
                                )
                                .flatMap(existing -> {
                                    existing.setCoverLetter(dto.coverLetter());
                                    existing.setUpdatedAt(LocalDateTime.now());
                                    return applicationRepositoryPort.save(existing);
                                })
                                .flatMap(saved ->
                                        applicationStatusRepositoryPort.findById(saved.getStatus())
                                                .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()))
                                                .map(status -> new ApplicationCreateResponseDto(
                                                        saved.getId(),
                                                        status.getApplicationStatusName().getValue(),
                                                        saved.getCreatedAt(),
                                                        saved.getUpdatedAt(),
                                                        saved.getCoverLetter()
                                                ))
                                )
                );
    }
}
