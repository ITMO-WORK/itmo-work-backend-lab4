package org.itmo.work.fileservice.application.usecase.register_application;

import org.itmo.work.fileservice.application.port.output.ApplicationRegistryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RegisterApplicationUseCaseTest {

    @Mock
    ApplicationRegistryPort applicationRegistryPort;

    @InjectMocks
    RegisterApplicationUseCase useCase;

    @Test
    void register_delegatesToRegistryPort_withSameArgs() {
        UUID applicationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        OffsetDateTime time = OffsetDateTime.of(2025, 12, 23, 17, 50, 0, 0, ZoneOffset.UTC);

        useCase.register(applicationId, ownerId, time);

        verify(applicationRegistryPort).register(applicationId, ownerId, time);
        verifyNoMoreInteractions(applicationRegistryPort);
    }
}