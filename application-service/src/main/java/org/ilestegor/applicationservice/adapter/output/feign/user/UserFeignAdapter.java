package org.ilestegor.applicationservice.adapter.output.feign.user;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.output.feign.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.exception.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFeignAdapter implements UserPort {

    private final UserClient userClient;

    @Override
    public Mono<UserResponseDto> checkUserExists(UUID userId, String token) {
        return Mono.fromCallable(() -> userClient.isUserExistsById(userId, token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(FeignException.NotFound.class, ex -> new UserNotFoundException());
    }
}
