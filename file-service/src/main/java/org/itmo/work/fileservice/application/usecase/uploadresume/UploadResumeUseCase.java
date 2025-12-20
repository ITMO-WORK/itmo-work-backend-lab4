package org.itmo.work.fileservice.application.usecase.uploadresume;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.adapter.output.kafka.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.application.port.input.UploadResumePort;
import org.itmo.work.fileservice.application.port.output.FileEventPublisherPort;
import org.itmo.work.fileservice.application.port.output.FileRepositoryPort;
import org.itmo.work.fileservice.application.port.output.FileStoragePort;
import org.itmo.work.fileservice.application.port.output.FileStoragePropsPort;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeRequest;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeResponse;

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

    @Override
    public UploadResumeResponse uploadResume(MultipartFile file, UUID replacedField, UploadResumeRequest uploadRequest) {
        UUID actuallyReplaced = null;

        if (replacedField != null) {
            StoredFile old = storedFileRepositoryPort.findById(replacedField)
                    .orElseThrow(() -> new IllegalArgumentException("replaced Id not found: " + replacedField));

            fileStoragePort.delete(old.getBucket(), old.getObjectKey());
            storedFileRepositoryPort.delete(old);
            actuallyReplaced = old.getId();
        }

        String bucket = fileStoragePropsPort.bucket();
        String objectKey = "resume/" + UUID.randomUUID();

        fileStoragePort.put(bucket, objectKey, file);

        StoredFile saved = storedFileRepositoryPort.save(
                StoredFile.builder()
                        .bucket(bucket)
                        .entityId(uploadRequest.applicationId())
                        .entityType(EntityType.APPLICATION)
                        .ownerId(uploadRequest.userId())
                        .purpose(FilePurpose.APPLICATION_RESUME)
                        .objectKey(objectKey)
                        .originalFileName(java.util.Optional.ofNullable(file.getOriginalFilename()).orElse("file"))
                        .contentType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .createdAt(java.time.Instant.now())
                        .build()
        );

        // 4) publish event (не Kafka DTO, а “внутренний” event)
        fileEventPublisherPort.publishFileUploadedEvent(
                new FileUploadEvent(
                        saved.getId(),
                        saved.getOwnerId(),
                        saved.getOriginalFileName(),
                        saved.getContentType(),
                        saved.getCreatedAt()
                )
        );

        return new UploadResumeResponse(saved.getId(), actuallyReplaced);
    }
}
