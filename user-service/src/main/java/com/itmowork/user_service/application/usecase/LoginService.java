package com.itmowork.user_service.application.usecase;


import com.itmowork.user_service.application.dto.LoginCommand;
import com.itmowork.user_service.application.dto.result.AuthResult;
import com.itmowork.user_service.application.port.in.LoginUseCase;
import com.itmowork.user_service.application.port.out.AuthPort;
import com.itmowork.user_service.application.port.out.TokenGeneratorPort;
import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final AuthPort authPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Override
    public Mono<AuthResult> login(LoginCommand command) {
        String email = command.email();
        String password = command.password();

        return authPort.authenticate(email, password)
                .then(userRepositoryPort.findByEmail(email)
                        .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")))
                )
                .flatMap(user -> {
                    List<String> roles = user.getRole().stream()
                            .map(r -> r.getRoleName().name())
                            .toList();

                    return tokenGeneratorPort.generateAccessToken(user.getId(), user.getEmail(), roles)
                            .map(token -> new AuthResult(user.getId(), token));
                });
    }
}