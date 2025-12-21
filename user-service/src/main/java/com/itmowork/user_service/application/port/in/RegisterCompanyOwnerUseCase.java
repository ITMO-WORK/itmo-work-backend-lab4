package com.itmowork.user_service.application.port.in;

import com.itmowork.user_service.application.dto.RegisterCompanyOwnerCommand;
import com.itmowork.user_service.application.dto.result.AuthResult;
import reactor.core.publisher.Mono;

public interface RegisterCompanyOwnerUseCase {
    Mono<AuthResult> registerCompanyOwner(RegisterCompanyOwnerCommand command);
}