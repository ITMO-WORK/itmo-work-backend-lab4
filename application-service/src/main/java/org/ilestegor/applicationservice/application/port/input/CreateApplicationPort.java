package org.ilestegor.applicationservice.application.port.input;

import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.ApplicationCreateResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CreateApplicationPort {
    Mono<ApplicationCreateResponseDto> createApplication(UUID vacancyId, ApplicationCreateRequestDto dto, FilePart resume, UUID replacedField);
}
