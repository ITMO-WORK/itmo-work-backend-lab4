package org.itmo.work.fileservice.application.port.input;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RegisterApplicationPort {
    void register(UUID applicationId, UUID ownerId, OffsetDateTime time);
}
