package org.itmo.work.fileservice.adapter.output.persistance;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.application.port.output.FileRepositoryPort;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpringDataFileRepositoryAdapter implements FileRepositoryPort {

    private final StoredFileRepository storedFileRepository;

    @Override
    public Optional<StoredFile> findById(UUID id) {
        return storedFileRepository.findById(id);
    }

    @Override
    public StoredFile save(StoredFile storedFile) {
        return storedFileRepository.save(storedFile);
    }

    @Override
    public void delete(StoredFile storedFile) {
        storedFileRepository.delete(storedFile);
    }
}
