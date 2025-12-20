package org.itmo.work.fileservice.application.port.output;

import org.itmo.work.fileservice.domain.model.StoredFile;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface FileRepositoryPort {

    Optional<StoredFile> findById(UUID id);
    StoredFile save(StoredFile storedFile);
    void delete(StoredFile storedFile);
}
