package com.itmowork.user_service.application.port.in;

import com.itmowork.user_service.application.dto.query.GetUserByIdQuery;
import com.itmowork.user_service.application.dto.result.UserResult;
import reactor.core.publisher.Mono;

public interface GetUserByIdUseCase {
    Mono<UserResult> getById(GetUserByIdQuery query);
}