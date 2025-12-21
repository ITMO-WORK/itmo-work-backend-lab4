package org.ilestegor.applicationservice.application.port.input;

import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationStatusUpdateResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateApplicationStatusPort {

    Mono<ApplicationStatusUpdateResponseDto> updateApplicationStatus(UUID applicationId, ApplicationStatusUpdateRequestDto applicationStatusUpdateRequestDto);
}
