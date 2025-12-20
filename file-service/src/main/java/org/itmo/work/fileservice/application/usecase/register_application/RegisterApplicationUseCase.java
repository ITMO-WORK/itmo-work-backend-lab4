package org.itmo.work.fileservice.application.usecase.register_application;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.application.port.input.RegisterApplicationPort;
import org.itmo.work.fileservice.application.port.output.ApplicationRegistryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterApplicationUseCase implements RegisterApplicationPort {
    private final ApplicationRegistryPort applicationRegistryPort;

    @Override
    public void register(UUID applicationId, UUID ownerId, OffsetDateTime time) {
        applicationRegistryPort.register(applicationId, ownerId, time);
    }
}
