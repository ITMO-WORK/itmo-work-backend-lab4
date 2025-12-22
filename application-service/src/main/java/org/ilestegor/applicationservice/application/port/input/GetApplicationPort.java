package org.ilestegor.applicationservice.application.port.input;

import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetApplicationPort {
    Mono<ApplicationDto> getApplicationByApplicationId();
}
