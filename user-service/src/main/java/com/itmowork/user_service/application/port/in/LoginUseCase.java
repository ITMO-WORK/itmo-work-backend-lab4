package com.itmowork.user_service.application.port.in;

import com.itmowork.user_service.application.dto.LoginCommand;
import com.itmowork.user_service.application.dto.result.AuthResult;
import reactor.core.publisher.Mono;

public interface LoginUseCase {
    Mono<AuthResult> login(LoginCommand command);
}
