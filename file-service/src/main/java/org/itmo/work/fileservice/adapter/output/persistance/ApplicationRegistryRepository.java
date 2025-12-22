package org.itmo.work.fileservice.adapter.output.persistance;

import org.itmo.work.fileservice.domain.model.ApplicationRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRegistryRepository extends JpaRepository<ApplicationRegistry, UUID> {
    ApplicationRegistry findApplicationRegistryByApplicationId(UUID applicationId);
}
