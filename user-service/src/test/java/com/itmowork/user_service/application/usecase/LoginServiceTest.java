package com.itmowork.user_service.application.usecase;

import com.itmowork.user_service.application.dto.LoginCommand;
import com.itmowork.user_service.application.port.out.AuthPort;
import com.itmowork.user_service.application.port.out.TokenGeneratorPort;
import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import com.itmowork.user_service.domain.model.Role;
import com.itmowork.user_service.domain.model.RoleName;
import com.itmowork.user_service.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private AuthPort authPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private TokenGeneratorPort tokenGeneratorPort;

    @Test
    void login_happyPath_shouldReturnAuthResult() {
        String email = "owner@example.com";
        String password = "pass123";
        UUID userId = UUID.randomUUID();

        Role role = new Role(RoleName.ROLE_COMPANY_OWNER);

        User user = User.builder()
                .id(userId)
                .email(email)
                .role(List.of(role))
                .build();

        when(authPort.authenticate(email, password)).thenReturn(Mono.empty());
        when(userRepositoryPort.findByEmail(email)).thenReturn(Mono.just(user));
        when(tokenGeneratorPort.generateAccessToken(eq(userId), eq(email), eq(List.of("ROLE_COMPANY_OWNER"))))
                .thenReturn(Mono.just("jwt-token"));

        LoginService service = new LoginService(authPort, userRepositoryPort, tokenGeneratorPort);

        StepVerifier.create(service.login(new LoginCommand(email, password)))
                .assertNext(r -> {
                    org.assertj.core.api.Assertions.assertThat(r.id()).isEqualTo(userId);
                    org.assertj.core.api.Assertions.assertThat(r.token()).isEqualTo("jwt-token");
                })
                .verifyComplete();

        verify(authPort).authenticate(email, password);
        verify(userRepositoryPort).findByEmail(email);
        verify(tokenGeneratorPort).generateAccessToken(userId, email, List.of("ROLE_COMPANY_OWNER"));
        verifyNoMoreInteractions(authPort, userRepositoryPort, tokenGeneratorPort);
    }

    @Test
    void login_whenUserNotFound_shouldErrorBadCredentials() {
        String email = "missing@example.com";
        String password = "pass123";

        when(authPort.authenticate(email, password)).thenReturn(Mono.empty());
        when(userRepositoryPort.findByEmail(email)).thenReturn(Mono.empty());

        LoginService service = new LoginService(authPort, userRepositoryPort, tokenGeneratorPort);

        StepVerifier.create(service.login(new LoginCommand(email, password)))
                .expectErrorSatisfies(ex -> {
                    org.assertj.core.api.Assertions.assertThat(ex).isInstanceOf(BadCredentialsException.class);
                    org.assertj.core.api.Assertions.assertThat(ex.getMessage()).isEqualTo("User not found");
                })
                .verify();

        verify(authPort).authenticate(email, password);
        verify(userRepositoryPort).findByEmail(email);
        verifyNoInteractions(tokenGeneratorPort);
        verifyNoMoreInteractions(authPort, userRepositoryPort);
    }

    @Test
    void login_whenAuthFails_shouldPropagateError_andNotGenerateToken() {
        String email = "owner@example.com";
        String password = "wrong";

        RuntimeException authError = new RuntimeException("auth failed");

        // ВАЖНО: иначе Mockito вернёт null и упадём на switchIfEmpty(...)
        when(userRepositoryPort.findByEmail(email)).thenReturn(Mono.empty());
        when(authPort.authenticate(email, password)).thenReturn(Mono.error(authError));

        LoginService service = new LoginService(authPort, userRepositoryPort, tokenGeneratorPort);

        StepVerifier.create(service.login(new LoginCommand(email, password)))
                .expectErrorMatches(ex -> ex == authError)
                .verify();

        verify(authPort).authenticate(email, password);

        // token не должен вызываться, потому что auth упал
        verifyNoInteractions(tokenGeneratorPort);

        // findByEmail МОЖЕТ быть вызван при построении цепочки (из-за then(userRepositoryPort.findByEmail(...)))
        // поэтому НЕ используем verifyNoInteractions(userRepositoryPort)
        verify(userRepositoryPort).findByEmail(email);

        verifyNoMoreInteractions(authPort, userRepositoryPort);
    }
}
