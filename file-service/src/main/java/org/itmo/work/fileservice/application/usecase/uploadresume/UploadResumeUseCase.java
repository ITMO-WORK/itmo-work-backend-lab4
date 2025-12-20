package org.itmo.work.fileservice.application.usecase.uploadresume;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.adapter.output.kafka.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.application.port.input.UploadResumePort;
import org.itmo.work.fileservice.application.port.output.*;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeResponse;

import org.itmo.work.fileservice.domain.exception.ApplicationNotFoundException;
import org.itmo.work.fileservice.domain.exception.ResumeAlreadyExistsException;
import org.itmo.work.fileservice.domain.model.EntityType;
import org.itmo.work.fileservice.domain.model.FilePurpose;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadResumeUseCase implements UploadResumePort {
    private final FileRepositoryPort storedFileRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final FileEventPublisherPort fileEventPublisherPort;
    private final FileStoragePropsPort fileStoragePropsPort;
    private final CurrentUserPort currentUserPort;
    private final ApplicationRegistryPort applicationRegistryPort;

    @Override
    public UploadResumeResponse uploadResume(MultipartFile file, UUID applicationId) {

        System.out.println(applicationId);

        boolean exists = applicationRegistryPort.exists(applicationId);
        if (!exists) throw new ApplicationNotFoundException();

        if (storedFileRepositoryPort.checkResumeAlreadyExists(applicationId))
            throw new ResumeAlreadyExistsException();


        String bucket = fileStoragePropsPort.bucket();
        String objectKey = "resume/" + UUID.randomUUID();

        fileStoragePort.put(bucket, objectKey, file);

        StoredFile saved = storedFileRepositoryPort.save(
                StoredFile.builder()
                        .bucket(bucket)
                        .entityId(applicationId)
                        .entityType(EntityType.APPLICATION)
                        .ownerId(currentUserPort.getCurrentUserId())
                        .purpose(FilePurpose.APPLICATION_RESUME)
                        .objectKey(objectKey)
                        .originalFileName(java.util.Optional.ofNullable(file.getOriginalFilename()).orElse("file"))
                        .contentType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .createdAt(java.time.Instant.now())
                        .build()
        );

        fileEventPublisherPort.publishFileUploadedEvent(
                new FileUploadEvent(
                        saved.getId(),
                        saved.getOwnerId(),
                        saved.getOriginalFileName(),
                        saved.getContentType(),
                        saved.getCreatedAt()
                )
        );

        return new UploadResumeResponse(saved.getId());
    }
}
