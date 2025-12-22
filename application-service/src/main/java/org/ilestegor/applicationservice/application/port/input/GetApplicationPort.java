package org.ilestegor.applicationservice.application.port.input;

import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.response.GetMyApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetApplicationPort {
    Mono<Page<GetMyApplicationResponse>> getApplicationByApplicationId(Pageable pageable);
}
