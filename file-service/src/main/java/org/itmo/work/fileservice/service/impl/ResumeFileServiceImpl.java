package org.itmo.work.fileservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.config.MinioProperties;
import org.itmo.work.fileservice.dto.response.UploadResumeResponse;
import org.itmo.work.fileservice.infrastructure.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.infrastructure.file.FileEventPublisher;
import org.itmo.work.fileservice.model.FilePurpose;
import org.itmo.work.fileservice.model.StoredFile;
import org.itmo.work.fileservice.repository.StoredFileRepository;
import org.itmo.work.fileservice.service.ResumeFileService;
import org.itmo.work.fileservice.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeFileServiceImpl implements ResumeFileService {
    private final StoredFileRepository storedFileRepository;
    private final StorageService storageService;
    private final MinioProperties minioProperties;
    private final FileEventPublisher finalEventPublisher;


    @Override
    public UploadResumeResponse uploadResume(MultipartFile file, UUID replacedField) {
        UUID actuallyReplaced = null;
        if (replacedField != null){
            StoredFile old = storedFileRepository.findById(replacedField)
                    .orElseThrow(() -> new IllegalArgumentException("replaced Id not found: " + replacedField));

            storageService.delete(old.getBucket(), old.getObjectKey());
            storedFileRepository.delete(old);
            actuallyReplaced = old.getId();
        }
        String bucket = minioProperties.bucket();
        String objectKey = "resume/" + UUID.randomUUID();

        storageService.put(bucket, objectKey, file);

        StoredFile saved = storedFileRepository.save(
                StoredFile.builder().bucket(bucket)
                        .entityId(UUID.randomUUID())
                        .entityType(FilePurpose.APPLICATION_RESUME.getValue())
                        .ownerId(UUID.randomUUID())
                        .purpose(FilePurpose.APPLICATION_RESUME)
                        .objectKey(objectKey)
                        .originalFileName(Optional.ofNullable(file.getOriginalFilename()).orElse("file"))
                        .contentType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .createdAt(Instant.now())
                        .build()
        );
        finalEventPublisher.publishFileUploadedEvent(
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
