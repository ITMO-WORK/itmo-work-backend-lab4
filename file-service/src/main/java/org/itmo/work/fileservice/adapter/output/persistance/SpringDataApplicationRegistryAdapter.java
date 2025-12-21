package org.itmo.work.fileservice.adapter.output.persistance;

import jakarta.ws.rs.core.Application;
import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.application.port.output.ApplicationRegistryPort;
import org.itmo.work.fileservice.domain.model.ApplicationRegistry;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpringDataApplicationRegistryAdapter implements ApplicationRegistryPort {
    private final ApplicationRegistryRepository applicationRegistryRepository;

    @Override
    public void register(UUID applicationId, UUID ownerId, OffsetDateTime createdAt) {
        if (applicationRegistryRepository.existsById(applicationId)) return;
        applicationRegistryRepository.save(ApplicationRegistry.builder().applicationId(applicationId)
                .ownerId(ownerId)
                .createdAt(createdAt).build());
    }

    @Override
    public Boolean exists(UUID applicationId) {
        return applicationRegistryRepository.existsById(applicationId);
    }
}
