package org.itmo.work.fileservice.application.port.output;

import org.itmo.work.fileservice.domain.model.StoredFile;

import java.util.Optional;
import java.util.UUID;

public interface FileRepositoryPort {

    Boolean checkResumeAlreadyExists(UUID applicationId);
    Optional<StoredFile> getResumeByApplicationId(UUID applicationId);
    StoredFile save(StoredFile storedFile);
    void delete(StoredFile storedFile);
}
