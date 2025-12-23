package org.itmo.work.fileservice.application.usecase.uploadresume;

import org.itmo.work.fileservice.adapter.output.kafka.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.application.port.output.*;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeRequest;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeResponse;
import org.itmo.work.fileservice.domain.exception.ApplicationNotFoundException;
import org.itmo.work.fileservice.domain.exception.ResumeAlreadyExistsException;
import org.itmo.work.fileservice.domain.model.EntityType;
import org.itmo.work.fileservice.domain.model.FilePurpose;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadResumeUseCaseTest {

    @Mock
    FileRepositoryPort storedFileRepositoryPort;
    @Mock
    FileStoragePort fileStoragePort;
    @Mock
    FileEventPublisherPort fileEventPublisherPort;
    @Mock
    FileStoragePropsPort fileStoragePropsPort;
    @Mock
    CurrentUserPort currentUserPort;
    @Mock
    ApplicationRegistryPort applicationRegistryPort;

    @Mock
    MultipartFile multipartFile;

    @InjectMocks
    UploadResumeUseCase useCase;

    @Test
    void uploadResume_whenApplicationNotExists_shouldThrowApplicationNotFound_andDoNothingElse() {
        UUID applicationId = UUID.randomUUID();
        when(applicationRegistryPort.exists(applicationId)).thenReturn(false);

        assertThrows(ApplicationNotFoundException.class,
                () -> useCase.uploadResume(multipartFile, applicationId));

        verify(applicationRegistryPort).exists(applicationId);
        verifyNoInteractions(storedFileRepositoryPort, fileStoragePort, fileEventPublisherPort, fileStoragePropsPort, currentUserPort);
    }

    @Test
    void uploadResume_whenResumeAlreadyExists_shouldThrowResumeAlreadyExists_andNotUploadNotPublish() {
        UUID applicationId = UUID.randomUUID();
        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.checkResumeAlreadyExists(applicationId)).thenReturn(true);

        assertThrows(ResumeAlreadyExistsException.class,
                () -> useCase.uploadResume(multipartFile, applicationId));

        verify(applicationRegistryPort).exists(applicationId);
        verify(storedFileRepositoryPort).checkResumeAlreadyExists(applicationId);

        verifyNoInteractions(fileStoragePropsPort, fileStoragePort, currentUserPort, fileEventPublisherPort);
        verify(storedFileRepositoryPort, never()).save(any());
    }

    @Test
    void uploadResume_success_shouldPutSavePublishAndReturnId_andDefaultOriginalNameIfNull() {
        UUID applicationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-12-23T12:00:00Z");

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.checkResumeAlreadyExists(applicationId)).thenReturn(false);

        when(fileStoragePropsPort.bucket()).thenReturn("bucket-1");
        when(currentUserPort.getCurrentUserId()).thenReturn(ownerId);

        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(1234L);


        StoredFile saved = StoredFile.builder()
                .id(savedId)
                .bucket("bucket-1")
                .ownerId(ownerId)
                .entityId(applicationId)
                .entityType(EntityType.APPLICATION)
                .purpose(FilePurpose.APPLICATION_RESUME)
                .objectKey("resume/some-key")
                .originalFileName("file")
                .contentType("application/pdf")
                .sizeBytes(1234L)
                .createdAt(createdAt)
                .build();


        when(storedFileRepositoryPort.save(any(StoredFile.class))).thenReturn(saved);

        UploadResumeResponse response = useCase.uploadResume(multipartFile, applicationId);

        assertNotNull(response);
        assertEquals(savedId, response.fileId());


        ArgumentCaptor<String> bucketCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);

        verify(fileStoragePort).put(bucketCap.capture(), keyCap.capture(), eq(multipartFile));
        assertEquals("bucket-1", bucketCap.getValue());
        assertTrue(keyCap.getValue().startsWith("resume/"), "objectKey должен начинаться с resume/");


        ArgumentCaptor<StoredFile> storedFileCaptor = ArgumentCaptor.forClass(StoredFile.class);
        verify(storedFileRepositoryPort).save(storedFileCaptor.capture());
        StoredFile toSave = storedFileCaptor.getValue();

        assertNotNull(toSave);
        assertEquals("bucket-1", toSave.getBucket());
        assertEquals(applicationId, toSave.getEntityId());
        assertEquals(EntityType.APPLICATION, toSave.getEntityType());
        assertEquals(ownerId, toSave.getOwnerId());
        assertEquals(FilePurpose.APPLICATION_RESUME, toSave.getPurpose());
        assertNotNull(toSave.getObjectKey());
        assertTrue(toSave.getObjectKey().startsWith("resume/"));
        assertEquals("file", toSave.getOriginalFileName());
        assertEquals("application/pdf", toSave.getContentType());
        assertEquals(1234L, toSave.getSizeBytes());
        assertNotNull(toSave.getCreatedAt());


        ArgumentCaptor<FileUploadEvent> eventCaptor = ArgumentCaptor.forClass(FileUploadEvent.class);
        verify(fileEventPublisherPort).publishFileUploadedEvent(eventCaptor.capture());

        FileUploadEvent event = eventCaptor.getValue();
        assertNotNull(event);
        assertEquals(savedId, event.fileId());
        assertEquals(ownerId, event.userId());
        assertEquals("file", event.originalFileName());
        assertEquals("application/pdf", event.contentType());
        assertEquals(createdAt, event.uploadedAt());
    }

    @Test
    void record_shouldExposeFields_andEqualsHashCodeToStringWork() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UploadResumeRequest r1 = new UploadResumeRequest(appId, userId);
        UploadResumeRequest r2 = new UploadResumeRequest(appId, userId);


        assertEquals(appId, r1.applicationId());
        assertEquals(userId, r1.userId());


        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());


        assertTrue(r1.toString().contains(appId.toString()));
        assertTrue(r1.toString().contains(userId.toString()));
    }

    @Test
    void uploadResume_success_shouldKeepProvidedOriginalFilename() {
        UUID applicationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-12-23T12:00:00Z");

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.checkResumeAlreadyExists(applicationId)).thenReturn(false);

        when(fileStoragePropsPort.bucket()).thenReturn("bucket-z");
        when(currentUserPort.getCurrentUserId()).thenReturn(ownerId);

        when(multipartFile.getOriginalFilename()).thenReturn("cv.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(10L);

        StoredFile saved = StoredFile.builder()
                .id(savedId)
                .ownerId(ownerId)
                .originalFileName("cv.pdf")
                .contentType("application/pdf")
                .createdAt(createdAt)
                .build();

        when(storedFileRepositoryPort.save(any(StoredFile.class))).thenReturn(saved);

        UploadResumeResponse response = useCase.uploadResume(multipartFile, applicationId);
        assertEquals(savedId, response.fileId());

        ArgumentCaptor<StoredFile> storedFileCaptor = ArgumentCaptor.forClass(StoredFile.class);
        verify(storedFileRepositoryPort).save(storedFileCaptor.capture());
        assertEquals("cv.pdf", storedFileCaptor.getValue().getOriginalFileName());

        ArgumentCaptor<FileUploadEvent> eventCaptor = ArgumentCaptor.forClass(FileUploadEvent.class);
        verify(fileEventPublisherPort).publishFileUploadedEvent(eventCaptor.capture());
        assertEquals("cv.pdf", eventCaptor.getValue().originalFileName());
    }
}