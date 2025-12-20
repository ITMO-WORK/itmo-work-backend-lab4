package com.itmowork.user_service.application.usecase;

import com.itmowork.user_service.application.dto.RegisterCompanyOwnerCommand;
import com.itmowork.user_service.application.dto.result.AuthResult;
import com.itmowork.user_service.application.port.in.RegisterCompanyOwnerUseCase;
import com.itmowork.user_service.application.port.out.PasswordHasherPort;
import com.itmowork.user_service.application.port.out.RoleRepositoryPort;
import com.itmowork.user_service.application.port.out.TokenGeneratorPort;
import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import com.itmowork.user_service.domain.exception.exceptions.UserAlreadyExistsException;
import com.itmowork.user_service.domain.model.RoleName;
import com.itmowork.user_service.domain.model.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import javax.management.relation.RoleNotFoundException;
import java.util.List;

@RequiredArgsConstructor
public class RegisterCompanyOwnerService implements RegisterCompanyOwnerUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Override
    public Mono<AuthResult> registerCompanyOwner(RegisterCompanyOwnerCommand command) {
        String email = command.email();

        return userRepositoryPort.findByEmail(email)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("User already exists"));
                    }

                    return roleRepositoryPort.findByRoleName(RoleName.ROLE_COMPANY_OWNER)
                            .switchIfEmpty(Mono.error(new RoleNotFoundException("Default role ROLE_COMPANY_OWNER not found")))
                            .flatMap(defaultRole -> {
                                User newUser = User.builder()
                                        .fullName(command.fullName())
                                        .email(email)
                                        .password(passwordHasherPort.encode(command.password()))
                                        .role(List.of(defaultRole))
                                        .build();

                                return userRepositoryPort.save(newUser)
                                        .flatMap(saved -> {
                                            List<String> roles = saved.getRole().stream()
                                                    .map(r -> r.getRoleName().name())
                                                    .toList();

                                            return tokenGeneratorPort.generateAccessToken(saved.getId(), saved.getEmail(), roles)
                                                    .map(token -> new AuthResult(saved.getId(), token));
                                        });
                            });
                });
    }
}