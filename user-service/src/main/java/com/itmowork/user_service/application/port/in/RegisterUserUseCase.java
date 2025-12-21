package com.itmowork.user_service.application.port.in;

import com.itmowork.user_service.application.dto.RegisterUserCommand;
import com.itmowork.user_service.application.dto.result.AuthResult;
import reactor.core.publisher.Mono;

public interface RegisterUserUseCase {
    Mono<AuthResult> register(RegisterUserCommand command);
}
