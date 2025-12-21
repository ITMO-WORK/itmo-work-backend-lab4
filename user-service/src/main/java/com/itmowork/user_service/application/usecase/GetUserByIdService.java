package com.itmowork.user_service.application.usecase;

import com.itmowork.user_service.application.dto.query.GetUserByIdQuery;
import com.itmowork.user_service.application.dto.result.UserResult;
import com.itmowork.user_service.application.port.in.GetUserByIdUseCase;
import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import com.itmowork.user_service.domain.exception.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetUserByIdService implements GetUserByIdUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Mono<UserResult> getById(GetUserByIdQuery query) {
        return userRepositoryPort.findById(query.id())
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with id=" + query.id() + " not found")))
                .map(u -> new UserResult(u.getId(), u.getFullName(), u.getEmail()));
    }
}