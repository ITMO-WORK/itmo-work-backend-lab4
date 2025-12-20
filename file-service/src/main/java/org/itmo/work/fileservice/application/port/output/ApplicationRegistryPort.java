package org.itmo.work.fileservice.application.port.output;

import org.itmo.work.fileservice.domain.model.StoredFile;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRegistryPort {
    void register(UUID applicationId, UUID ownerId, OffsetDateTime createdAt);
    Boolean exists(UUID applicationId);
}
