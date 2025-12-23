package com.itmowork.user_service.application.usecase;

import com.itmowork.user_service.application.dto.query.GetUserByIdQuery;
import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import com.itmowork.user_service.domain.exception.exceptions.UserNotFoundException;
import com.itmowork.user_service.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserByIdServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Test
    void getById_whenUserExists_shouldReturnUserResult() {
        UUID id = UUID.randomUUID();

        User user = User.builder()
                .id(id)
                .fullName("Mariia Chmurova")
                .email("mariia@example.com")
                .build();

        when(userRepositoryPort.findById(id)).thenReturn(Mono.just(user));

        GetUserByIdService service = new GetUserByIdService(userRepositoryPort);

        StepVerifier.create(service.getById(new GetUserByIdQuery(id)))
                .assertNext(r -> {
                    org.assertj.core.api.Assertions.assertThat(r.id()).isEqualTo(id);
                    org.assertj.core.api.Assertions.assertThat(r.fullName()).isEqualTo("Mariia Chmurova");
                    org.assertj.core.api.Assertions.assertThat(r.email()).isEqualTo("mariia@example.com");
                })
                .verifyComplete();

        verify(userRepositoryPort).findById(id);
        verifyNoMoreInteractions(userRepositoryPort);
    }

    @Test
    void getById_whenUserMissing_shouldErrorUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepositoryPort.findById(id)).thenReturn(Mono.empty());

        GetUserByIdService service = new GetUserByIdService(userRepositoryPort);

        StepVerifier.create(service.getById(new GetUserByIdQuery(id)))
                .expectErrorSatisfies(ex -> {
                    org.assertj.core.api.Assertions.assertThat(ex).isInstanceOf(UserNotFoundException.class);
                    org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                            .isEqualTo("User with id=" + id + " not found");
                })
                .verify();

        verify(userRepositoryPort).findById(id);
        verifyNoMoreInteractions(userRepositoryPort);
    }
}
