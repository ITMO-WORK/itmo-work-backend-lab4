package org.itmo.work.fileservice.application.usecase.updateresume;

import org.itmo.work.fileservice.application.port.output.ApplicationRegistryPort;
import org.itmo.work.fileservice.application.port.output.CurrentUserPort;
import org.itmo.work.fileservice.application.port.output.FileRepositoryPort;
import org.itmo.work.fileservice.application.port.output.FileStoragePort;
import org.itmo.work.fileservice.application.port.output.FileStoragePropsPort;
import org.itmo.work.fileservice.application.usecase.updateresume.dto.UpdateResumeResponse;
import org.itmo.work.fileservice.domain.exception.ApplicationNotFoundException;
import org.itmo.work.fileservice.domain.exception.ResumeNotFoundException;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateResumeUseCaseTest {

    @Mock FileRepositoryPort storedFileRepositoryPort;
    @Mock FileStoragePort fileStoragePort;
    @Mock FileStoragePropsPort fileStoragePropsPort;
    @Mock ApplicationRegistryPort applicationRegistryPort;
    @Mock CurrentUserPort currentUserPort;

    @Mock MultipartFile multipartFile;

    @InjectMocks UpdateResumeUseCase useCase;

    @Test
    void updateResume_whenApplicationNotExists_shouldThrowApplicationNotFound_andDoNothingElse() {
        UUID applicationId = UUID.randomUUID();
        when(applicationRegistryPort.exists(applicationId)).thenReturn(false);

        assertThrows(ApplicationNotFoundException.class,
                () -> useCase.updateResume(applicationId, multipartFile));

        verify(applicationRegistryPort).exists(applicationId);
        verifyNoInteractions(storedFileRepositoryPort, fileStoragePort, fileStoragePropsPort, currentUserPort);
    }

    @Test
    void updateResume_whenResumeRecordMissing_shouldThrowApplicationNotFound() {
        UUID applicationId = UUID.randomUUID();
        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> useCase.updateResume(applicationId, multipartFile));

        verify(applicationRegistryPort).exists(applicationId);
        verify(storedFileRepositoryPort).getResumeByApplicationId(applicationId);
        verifyNoInteractions(fileStoragePort, fileStoragePropsPort, currentUserPort);
        verify(storedFileRepositoryPort, never()).save(any());
    }

    @Test
    void updateResume_whenCurrentUserNotOwner_shouldThrowApplicationNotFound_andNotUploadNotSave() {
        UUID applicationId = UUID.randomUUID();

        StoredFile resume = mock(StoredFile.class);

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId))
                .thenReturn(Optional.of(resume));

        UUID fileId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        when(resume.getId()).thenReturn(fileId);
        when(currentUserPort.getCurrentUserId()).thenReturn(currentUserId);

        assertThrows(ApplicationNotFoundException.class,
                () -> useCase.updateResume(applicationId, multipartFile));

        verify(applicationRegistryPort).exists(applicationId);
        verify(storedFileRepositoryPort).getResumeByApplicationId(applicationId);


        verify(resume, atLeastOnce()).getId();
        verify(currentUserPort).getCurrentUserId();


        verifyNoInteractions(fileStoragePort, fileStoragePropsPort);
        verify(storedFileRepositoryPort, never()).save(any());
    }

    @Test
    void updateResume_whenOwnerIdNull_shouldThrowApplicationNotFound() {
        UUID applicationId = UUID.randomUUID();

        StoredFile resume = mock(StoredFile.class);

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId)).thenReturn(Optional.of(resume));


        assertThrows(ResumeNotFoundException.class,
                () -> useCase.updateResume(applicationId, multipartFile));

        verifyNoInteractions(fileStoragePort, fileStoragePropsPort);
        verify(storedFileRepositoryPort, never()).save(any());
    }

    @Test
    void updateResume_whenResumeIdNull_shouldThrowResumeNotFound_andNotUploadNotSave() {
        UUID applicationId = UUID.randomUUID();

        StoredFile resume = mock(StoredFile.class);

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId)).thenReturn(Optional.of(resume));

        UUID currentUserId = UUID.randomUUID();



        when(resume.getId()).thenReturn(null);

        assertThrows(ResumeNotFoundException.class,
                () -> useCase.updateResume(applicationId, multipartFile));

        verify(resume).getId();
        verifyNoInteractions(fileStoragePort, fileStoragePropsPort);
        verify(storedFileRepositoryPort, never()).save(any());
    }

    @Test
    void updateResume_success_whenOriginalFilenameNull_shouldUseDefaultResumeName_andPersistAndReturnResponse() {
        UUID applicationId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        StoredFile resume = mock(StoredFile.class);

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId)).thenReturn(Optional.of(resume));

        when(resume.getOwnerId()).thenReturn(currentUserId);
        when(currentUserPort.getCurrentUserId()).thenReturn(currentUserId);

        when(resume.getId()).thenReturn(fileId);
        when(resume.getObjectKey()).thenReturn("obj/key.pdf");

        when(fileStoragePropsPort.bucket()).thenReturn("bucket-1");

        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(1234L);

        UpdateResumeResponse resp = useCase.updateResume(applicationId, multipartFile);

        assertNotNull(resp);
        assertEquals(fileId, resp.fileId());
        assertEquals("resume", resp.fileName());

        verify(fileStoragePort).put("bucket-1", "obj/key.pdf", multipartFile);
        verify(resume).setOriginalFileName("resume");
        verify(resume).setContentType("application/pdf");
        verify(resume).setSizeBytes(1234L);
        verify(storedFileRepositoryPort).save(resume);
    }

    @Test
    void updateResume_success_whenOriginalFilenameBlank_shouldUseDefaultResumeName() {
        UUID applicationId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        StoredFile resume = mock(StoredFile.class);

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId)).thenReturn(Optional.of(resume));

        when(resume.getOwnerId()).thenReturn(currentUserId);
        when(currentUserPort.getCurrentUserId()).thenReturn(currentUserId);

        when(resume.getId()).thenReturn(fileId);
        when(resume.getObjectKey()).thenReturn("obj/key");

        when(fileStoragePropsPort.bucket()).thenReturn("bucket-x");

        when(multipartFile.getOriginalFilename()).thenReturn("   ");
        when(multipartFile.getContentType()).thenReturn(null);
        when(multipartFile.getSize()).thenReturn(0L);

        UpdateResumeResponse resp = useCase.updateResume(applicationId, multipartFile);

        assertEquals(fileId, resp.fileId());
        assertEquals("resume", resp.fileName());

        verify(fileStoragePort).put("bucket-x", "obj/key", multipartFile);
        verify(resume).setOriginalFileName("resume");
        verify(resume).setContentType(null);
        verify(resume).setSizeBytes(0L);
        verify(storedFileRepositoryPort).save(resume);
    }

    @Test
    void updateResume_success_whenOriginalFilenameProvided_shouldKeepIt_andPutUsesBucketAndKey() {
        UUID applicationId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        StoredFile resume = mock(StoredFile.class);

        when(applicationRegistryPort.exists(applicationId)).thenReturn(true);
        when(storedFileRepositoryPort.getResumeByApplicationId(applicationId)).thenReturn(Optional.of(resume));

        when(resume.getOwnerId()).thenReturn(currentUserId);
        when(currentUserPort.getCurrentUserId()).thenReturn(currentUserId);

        when(resume.getId()).thenReturn(fileId);
        when(resume.getObjectKey()).thenReturn("o/k");
        when(fileStoragePropsPort.bucket()).thenReturn("bucket-z");

        when(multipartFile.getOriginalFilename()).thenReturn("my_resume.pdf");
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(999L);

        UpdateResumeResponse resp = useCase.updateResume(applicationId, multipartFile);

        assertEquals(fileId, resp.fileId());
        assertEquals("my_resume.pdf", resp.fileName());

        ArgumentCaptor<String> bucketCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        verify(fileStoragePort).put(bucketCap.capture(), keyCap.capture(), eq(multipartFile));
        assertEquals("bucket-z", bucketCap.getValue());
        assertEquals("o/k", keyCap.getValue());

        verify(resume).setOriginalFileName("my_resume.pdf");
        verify(resume).setContentType("application/pdf");
        verify(resume).setSizeBytes(999L);
        verify(storedFileRepositoryPort).save(resume);
    }
}