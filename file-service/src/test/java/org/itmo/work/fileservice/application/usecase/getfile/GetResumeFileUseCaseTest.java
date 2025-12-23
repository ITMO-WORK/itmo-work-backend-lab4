package org.itmo.work.fileservice.application.usecase.getfile;

import org.itmo.work.fileservice.application.port.output.FileRepositoryPort;
import org.itmo.work.fileservice.application.port.output.FileStoragePort;
import org.itmo.work.fileservice.application.port.output.FileStoragePropsPort;
import org.itmo.work.fileservice.domain.exception.ResumeNotFoundException;
import org.itmo.work.fileservice.domain.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetResumeFileUseCaseTest {

    @Mock
    FileStoragePort fileStoragePort;

    @Mock
    FileRepositoryPort fileRepositoryPort;

    @Mock
    FileStoragePropsPort fileStoragePropsPort;

    @InjectMocks
    GetResumeFileUseCase useCase;

    @Test
    void getDownloadUrl_fileFound_returnsPresignedUrl_andCallsPortsWithCorrectArgs() {

        UUID applicationId = UUID.randomUUID();

        StoredFile storedFile = mock(StoredFile.class);
        when(storedFile.getBucket()).thenReturn("bucket-1");
        when(storedFile.getObjectKey()).thenReturn("obj/key.pdf");

        when(fileRepositoryPort.getResumeByApplicationId(applicationId))
                .thenReturn(Optional.of(storedFile));

        Duration expiry = Duration.ofMinutes(10);
        when(fileStoragePropsPort.presignExpiry()).thenReturn(expiry);

        String expectedUrl = "https://storage/presigned";
        when(fileStoragePort.getPresignedGetUrl("bucket-1", "obj/key.pdf", expiry))
                .thenReturn(expectedUrl);


        String actual = useCase.getDownloadUrl(applicationId);


        assertEquals(expectedUrl, actual);

        verify(fileRepositoryPort).getResumeByApplicationId(applicationId);
        verify(fileStoragePropsPort).presignExpiry();
        verify(fileStoragePort).getPresignedGetUrl("bucket-1", "obj/key.pdf", expiry);

        verifyNoMoreInteractions(fileRepositoryPort, fileStoragePropsPort, fileStoragePort);
    }

    @Test
    void getDownloadUrl_fileNotFound_throwsResumeNotFound_andDoesNotCallStorageOrProps() {

        UUID applicationId = UUID.randomUUID();
        when(fileRepositoryPort.getResumeByApplicationId(applicationId))
                .thenReturn(Optional.empty());


        assertThrows(ResumeNotFoundException.class, () -> useCase.getDownloadUrl(applicationId));

        verify(fileRepositoryPort).getResumeByApplicationId(applicationId);
        verifyNoInteractions(fileStoragePropsPort);
        verifyNoInteractions(fileStoragePort);
        verifyNoMoreInteractions(fileRepositoryPort);
    }
}