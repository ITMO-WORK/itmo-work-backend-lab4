package org.itmo.work.fileservice.service.impl;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import org.itmo.work.fileservice.config.MinioProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinioStorageServiceTest {

    private MinioClient minioClient;
    private MinioProperties minioProperties;
    private MinioStorageService service;

    @BeforeEach
    void setUp() {
        minioClient = mock(MinioClient.class);
        minioProperties = mock(MinioProperties.class);
        service = new MinioStorageService(minioClient, minioProperties);
    }

    @Test
    void put_shouldCallMinioPutObject_withBucketFromProperties_andAllFields() throws Exception {
        // given
        when(minioProperties.bucket()).thenReturn("files-bucket");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        // when
        service.put("ignored-bucket-param", "resume/abc", file);

        // then
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(1)).putObject(captor.capture());

        PutObjectArgs args = captor.getValue();
        assertEquals("files-bucket", args.bucket());
        assertEquals("resume/abc", args.object());
        assertEquals("application/pdf", args.contentType());

        // bucket param игнорируется — важно проверить, что взяли именно из properties
        verify(minioProperties, times(1)).bucket();
        verifyNoMoreInteractions(minioProperties);
    }

    @Test
    void put_shouldWrapExceptionIntoRuntimeException() throws Exception {
        // given
        when(minioProperties.bucket()).thenReturn("files-bucket");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        doThrow(new IOException("boom"))
                .when(minioClient)
                .putObject(any(PutObjectArgs.class));

        // when
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.put("any", "resume/abc", file));

        // then
        assertEquals("Minio put failed", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
    }

    @Test
    void delete_shouldCallMinioRemoveObject_withBucketFromProperties() throws Exception {
        // given
        when(minioProperties.bucket()).thenReturn("files-bucket");

        // when
        service.delete("ignored-bucket-param", "resume/abc");

        // then
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient, times(1)).removeObject(captor.capture());

        RemoveObjectArgs args = captor.getValue();
        assertEquals("files-bucket", args.bucket());
        assertEquals("resume/abc", args.object());
    }

    @Test
    void delete_shouldWrapExceptionIntoRuntimeException() throws Exception {
        // given
        when(minioProperties.bucket()).thenReturn("files-bucket");
        doThrow(new RuntimeException("boom"))
                .when(minioClient)
                .removeObject(any(RemoveObjectArgs.class));

        // when
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.delete("any", "resume/abc"));

        // then
        assertEquals("Minio delete", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
    }

    @Test
    void getPresignedGetUrl_shouldReturnUrlFromMinio() throws Exception {
        // given
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio/presigned");

        // when
        String url = service.getPresignedGetUrl("bucket-x", "resume/abc", Duration.ofMinutes(10));

        // then
        assertEquals("http://minio/presigned", url);

        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());

        GetPresignedObjectUrlArgs args = captor.getValue();
        assertEquals("bucket-x", args.bucket());
        assertEquals("resume/abc", args.object());
        // expiry проверять не всегда удобно (там int seconds), но можно:
        assertEquals((int) Duration.ofMinutes(10).getSeconds(), args.expiry());
    }

    @Test
    void getPresignedGetUrl_shouldWrapExceptionIntoIllegalStateException_withHelpfulMessage() throws Exception {
        // given
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("boom"));

        // when
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.getPresignedGetUrl("bucket-x", "resume/abc", Duration.ofSeconds(60)));

        // then
        assertTrue(ex.getMessage().contains("Failed to generate presigned url"));
        assertTrue(ex.getMessage().contains("bucket=bucket-x"));
        assertTrue(ex.getMessage().contains("key=resume/abc"));
        assertNotNull(ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
    }
}