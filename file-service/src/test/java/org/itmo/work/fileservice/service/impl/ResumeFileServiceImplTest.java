package org.itmo.work.fileservice.service.impl;


import org.itmo.work.fileservice.config.MinioProperties;
import org.itmo.work.fileservice.dto.request.UploadRequest;
import org.itmo.work.fileservice.dto.response.UploadResumeResponse;
import org.itmo.work.fileservice.infrastructure.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.infrastructure.file.FileEventPublisher;
import org.itmo.work.fileservice.model.EntityType;
import org.itmo.work.fileservice.model.FilePurpose;
import org.itmo.work.fileservice.model.StoredFile;
import org.itmo.work.fileservice.repository.StoredFileRepository;
import org.itmo.work.fileservice.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeFileServiceImplTest {

    private StoredFileRepository repo;
    private StorageService storage;
    private MinioProperties props;
    private FileEventPublisher publisher;

    private ResumeFileServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(StoredFileRepository.class);
        storage = mock(StorageService.class);
        props = mock(MinioProperties.class);
        publisher = mock(FileEventPublisher.class);

        service = new ResumeFileServiceImpl(repo, storage, props, publisher);
    }

    @Test
    void uploadResume_whenFirstUpload_shouldPutSavePublish_andReturnResponse_withReplacedNull() {
        // given
        when(props.bucket()).thenReturn("bucket-1");

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "hello".getBytes()
        );

        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UploadRequest req = new UploadRequest(appId, userId);

        // save() должен вернуть объект с id, ownerId, createdAt и т.д.
        UUID savedId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-12-18T10:15:30Z");

        when(repo.save(any(StoredFile.class))).thenAnswer(inv -> {
            StoredFile toSave = inv.getArgument(0);

            // симулируем то, что JPA проставил id
            // (если у тебя StoredFile immutable — поменяй на builder/toBuilder)
            toSave.setId(savedId);
            toSave.setCreatedAt(createdAt);
            return toSave;
        });

        // when
        UploadResumeResponse resp = service.uploadResume(file, null, req);

        // then
        assertNotNull(resp);
        assertEquals(savedId, resp.fieldId());
        assertNull(resp.replacedField());

        // storage.put должен быть вызван с bucket из properties
        verify(storage, times(1)).put(eq("bucket-1"), startsWith("resume/"), eq(file));

        // проверим что сохраняем корректный StoredFile
        ArgumentCaptor<StoredFile> fileCaptor = ArgumentCaptor.forClass(StoredFile.class);
        verify(repo, times(1)).save(fileCaptor.capture());

        StoredFile saved = fileCaptor.getValue();
        assertEquals("bucket-1", saved.getBucket());
        assertEquals(appId, saved.getEntityId());
        assertEquals(EntityType.APPLICATION, saved.getEntityType());
        assertEquals(userId, saved.getOwnerId());
        assertEquals(FilePurpose.APPLICATION_RESUME, saved.getPurpose());
        assertNotNull(saved.getObjectKey());
        assertTrue(saved.getObjectKey().startsWith("resume/"));
        assertEquals("cv.pdf", saved.getOriginalFileName());
        assertEquals("application/pdf", saved.getContentType());
        assertEquals(file.getSize(), saved.getSizeBytes());
        assertNotNull(saved.getCreatedAt()); // в проде Instant.now()

        // publish event
        ArgumentCaptor<FileUploadEvent> eventCaptor = ArgumentCaptor.forClass(FileUploadEvent.class);
        verify(publisher, times(1)).publishFileUploadedEvent(eventCaptor.capture());

        FileUploadEvent event = eventCaptor.getValue();
        assertEquals(savedId, event.fileId());
        assertEquals(userId, event.userId());
        assertEquals("cv.pdf", event.originalFileName());
        assertEquals("application/pdf", event.contentType());
        assertEquals(createdAt, event.uploadedAt());

        // ветка replacedField==null => delete не должен быть
        verify(storage, never()).delete(anyString(), anyString());
        verify(repo, never()).delete(any(StoredFile.class));
    }

    @Test
    void uploadResume_whenReplacedFieldProvided_shouldDeleteOldFromStorage_andDeleteFromDb_andReturnActuallyReplaced() {
        // given
        when(props.bucket()).thenReturn("bucket-1");

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.pdf", "application/pdf", "new".getBytes()
        );

        UUID replacedId = UUID.randomUUID();
        StoredFile old = StoredFile.builder()
                .id(replacedId)
                .bucket("old-bucket")
                .objectKey("resume/old-key")
                .build();

        when(repo.findById(replacedId)).thenReturn(Optional.of(old));

        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UploadRequest req = new UploadRequest(appId, userId);

        UUID savedId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-12-18T10:15:30Z");

        when(repo.save(any(StoredFile.class))).thenAnswer(inv -> {
            StoredFile toSave = inv.getArgument(0);
            toSave.setId(savedId);
            toSave.setCreatedAt(createdAt);
            return toSave;
        });

        // when
        UploadResumeResponse resp = service.uploadResume(file, replacedId, req);

        // then
        assertEquals(savedId, resp.fieldId());
        assertEquals(replacedId, resp.replacedField());

        // старый файл удалили из minio и из БД
        verify(storage, times(1)).delete("old-bucket", "resume/old-key");
        verify(repo, times(1)).delete(old);

        // новый файл положили
        verify(storage, times(1)).put(eq("bucket-1"), startsWith("resume/"), eq(file));

        // событие отправили
        verify(publisher, times(1)).publishFileUploadedEvent(any(FileUploadEvent.class));
    }

    @Test
    void uploadResume_whenReplacedFieldNotFound_shouldThrowIllegalArgumentException() {
        // given
        UUID replacedId = UUID.randomUUID();
        when(repo.findById(replacedId)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "x".getBytes()
        );

        UploadRequest req = new UploadRequest(UUID.randomUUID(), UUID.randomUUID());

        // when
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadResume(file, replacedId, req)
        );

        // then
        assertTrue(ex.getMessage().contains("replaced Id not found"));

        // никаких side effects
        verify(storage, never()).delete(anyString(), anyString());
        verify(storage, never()).put(anyString(), anyString(), any());
        verify(repo, never()).delete(any());
        verify(repo, never()).save(any());
        verify(publisher, never()).publishFileUploadedEvent(any());
    }

    @Test
    void getDownloadUrl_shouldReturnPresignedUrl() {
        // given
        UUID fileId = UUID.randomUUID();
        StoredFile stored = StoredFile.builder()
                .id(fileId)
                .bucket("b1")
                .objectKey("resume/abc")
                .build();

        when(repo.findById(fileId)).thenReturn(Optional.of(stored));
        when(props.presignExpiry()).thenReturn(Duration.ofMinutes(10));
        when(storage.getPresignedGetUrl("b1", "resume/abc", Duration.ofMinutes(10)))
                .thenReturn("http://minio/presigned");

        // when
        String url = service.getDownloadUrl(fileId);

        // then
        assertEquals("http://minio/presigned", url);
        verify(repo, times(1)).findById(fileId);
        verify(storage, times(1)).getPresignedGetUrl("b1", "resume/abc", Duration.ofMinutes(10));
    }

    @Test
    void getDownloadUrl_whenFileNotFound_shouldThrowIllegalArgumentException() {
        // given
        UUID fileId = UUID.randomUUID();
        when(repo.findById(fileId)).thenReturn(Optional.empty());

        // when
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getDownloadUrl(fileId)
        );

        // then
        assertTrue(ex.getMessage().contains("File not found"));
        verify(storage, never()).getPresignedGetUrl(anyString(), anyString(), any());
    }

    @Test
    void uploadResume_whenOriginalFilenameNull_shouldFallbackToFile() throws Exception {
        // given
        when(props.bucket()).thenReturn("bucket-1");

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);                 // ключевое
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(5L);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("hello".getBytes()));

        UploadRequest req = new UploadRequest(UUID.randomUUID(), UUID.randomUUID());

        when(repo.save(any(StoredFile.class))).thenAnswer(inv -> {
            StoredFile toSave = inv.getArgument(0);
            toSave.setId(UUID.randomUUID());
            return toSave;
        });

        // when
        service.uploadResume(file, null, req);

        // then
        ArgumentCaptor<StoredFile> captor = ArgumentCaptor.forClass(StoredFile.class);
        verify(repo).save(captor.capture());
        assertEquals("file", captor.getValue().getOriginalFileName());
    }
}