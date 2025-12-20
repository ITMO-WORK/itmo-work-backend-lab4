package org.itmo.work.fileservice.application.usecase.getfile;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.application.port.input.GetResumeFilePort;
import org.itmo.work.fileservice.application.port.output.FileRepositoryPort;
import org.itmo.work.fileservice.application.port.output.FileStoragePort;
import org.itmo.work.fileservice.application.port.output.FileStoragePropsPort;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetResumeFileUseCase implements GetResumeFilePort {

    private final FileStoragePort fileStoragePort;
    private final FileRepositoryPort fileRepositoryPort;
    private final FileStoragePropsPort fileStoragePropsPort;

    @Override
    public String getDownloadUrl(UUID fileId) {
        StoredFile file = fileRepositoryPort.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        return fileStoragePort.getPresignedGetUrl(
                file.getBucket(),
                file.getObjectKey(),
                fileStoragePropsPort.presignExpiry()
        );
    }
}
