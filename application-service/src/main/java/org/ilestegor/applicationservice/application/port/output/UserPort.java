package org.ilestegor.applicationservice.application.port.output;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserPort {

    Mono<UserResponseDto> checkUserExists(UUID userId, String token);
}
