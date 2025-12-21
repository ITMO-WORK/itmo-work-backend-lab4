package org.ilestegor.applicationservice.application.port.input;

import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationCreateResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateApplicationPort {

    Mono<ApplicationCreateResponseDto> updateApplication(UUID applicationId, ApplicationCreateRequestDto applicationCreateRequestDto);
}
