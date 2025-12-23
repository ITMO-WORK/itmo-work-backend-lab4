package com.itmowork.user_service.application.usecase;

import com.itmowork.user_service.application.dto.RegisterUserCommand;
import com.itmowork.user_service.application.port.out.PasswordHasherPort;
import com.itmowork.user_service.application.port.out.RoleRepositoryPort;
import com.itmowork.user_service.application.port.out.TokenGeneratorPort;
import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import com.itmowork.user_service.domain.exception.exceptions.UserAlreadyExistsException;
import com.itmowork.user_service.domain.model.Role;
import com.itmowork.user_service.domain.model.RoleName;
import com.itmowork.user_service.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private RoleRepositoryPort roleRepositoryPort;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private TokenGeneratorPort tokenGeneratorPort;

    @Test
    void register_happyPath_shouldSaveUserAndReturnToken() {
        String email = "user@example.com";
        String password = "pass123";
        String encoded = "encoded";
        UUID savedId = UUID.randomUUID();

        Role defaultRole = new Role(RoleName.ROLE_USER);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Mono.empty());
        when(roleRepositoryPort.findByRoleName(RoleName.ROLE_USER)).thenReturn(Mono.just(defaultRole));
        when(passwordHasherPort.encode(password)).thenReturn(encoded);

        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(savedId);
            return Mono.just(u);
        });

        when(tokenGeneratorPort.generateAccessToken(savedId, email, List.of("ROLE_USER")))
                .thenReturn(Mono.just("jwt"));

        RegisterUserService service = new RegisterUserService(
                userRepositoryPort, roleRepositoryPort, passwordHasherPort, tokenGeneratorPort
        );

        RegisterUserCommand cmd = new RegisterUserCommand("Full Name", password, email);

        StepVerifier.create(service.register(cmd))
                .assertNext(r -> {
                    assertThat(r.id()).isEqualTo(savedId);
                    assertThat(r.token()).isEqualTo("jwt");
                })
                .verifyComplete();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getFullName()).isEqualTo("Full Name");
        assertThat(saved.getPassword()).isEqualTo(encoded);
        assertThat(saved.getRole())
                .hasSize(1)
                .first()
                .extracting(Role::getRoleName)
                .isEqualTo(RoleName.ROLE_USER);

        verify(userRepositoryPort).findByEmail(email);
        verify(roleRepositoryPort).findByRoleName(RoleName.ROLE_USER);
        verify(passwordHasherPort).encode(password);
        verify(tokenGeneratorPort).generateAccessToken(savedId, email, List.of("ROLE_USER"));
        verifyNoMoreInteractions(userRepositoryPort, roleRepositoryPort, passwordHasherPort, tokenGeneratorPort);
    }

    @Test
    void register_whenUserAlreadyExists_shouldError() {
        String email = "user@example.com";
        when(userRepositoryPort.findByEmail(email))
                .thenReturn(Mono.just(User.builder().email(email).build()));

        RegisterUserService service = new RegisterUserService(
                userRepositoryPort, roleRepositoryPort, passwordHasherPort, tokenGeneratorPort
        );

        RegisterUserCommand cmd = new RegisterUserCommand("Name", "pass", email);

        StepVerifier.create(service.register(cmd))
                .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(UserAlreadyExistsException.class))
                .verify();

        verify(userRepositoryPort).findByEmail(email);
        verifyNoInteractions(roleRepositoryPort, passwordHasherPort, tokenGeneratorPort);
        verifyNoMoreInteractions(userRepositoryPort);
    }

    @Test
    void register_whenDefaultRoleMissing_shouldErrorRoleNotFound() {
        String email = "user@example.com";

        when(userRepositoryPort.findByEmail(email)).thenReturn(Mono.empty());
        when(roleRepositoryPort.findByRoleName(RoleName.ROLE_USER)).thenReturn(Mono.empty());

        RegisterUserService service = new RegisterUserService(
                userRepositoryPort, roleRepositoryPort, passwordHasherPort, tokenGeneratorPort
        );

        RegisterUserCommand cmd = new RegisterUserCommand("Name", "pass", email);

        StepVerifier.create(service.register(cmd))
                .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(RoleNotFoundException.class))
                .verify();

        verify(userRepositoryPort).findByEmail(email);
        verify(roleRepositoryPort).findByRoleName(RoleName.ROLE_USER);
        verifyNoInteractions(passwordHasherPort, tokenGeneratorPort);
        verifyNoMoreInteractions(userRepositoryPort, roleRepositoryPort);
    }
}
