package org.itmo.work.fileservice.controller;


import io.minio.*;
import org.itmo.work.fileservice.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class FileIntegrationTest {

    @Container
    static GenericContainer<?> minio =
            new GenericContainer<>("minio/minio:latest")
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withCommand("server /data")
                    .withExposedPorts(9000);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        String endpoint = "http://" + minio.getHost() + ":" + minio.getMappedPort(9000);

        registry.add("minio.internal-endpoint", () -> endpoint);
        registry.add("minio.public-endpoint", () -> endpoint);
        registry.add("minio.access-key", () -> "minioadmin");
        registry.add("minio.secret-key", () -> "minioadmin");
        registry.add("minio.bucket", () -> "files");
        registry.add("minio.presign-expiry", () -> Duration.ofMinutes(5));
    }

    @Autowired
    MinioClient minioInternalClient;

    @Autowired
    StorageService storageService;

    @BeforeEach
    void setup() throws Exception {
        boolean exists = minioInternalClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket("files")
                        .build()
        );

        if (!exists) {
            minioInternalClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket("files")
                            .build()
            );
        }

        minioInternalClient.putObject(
                PutObjectArgs.builder()
                        .bucket("files")
                        .object("test.txt")
                        .stream(
                                new ByteArrayInputStream("hello".getBytes()),
                                5,
                                -1
                        )
                        .contentType("text/plain")
                        .build()
        );
    }

    @Test
    void presignedUrlShouldDownloadFile() throws Exception {
        String url = storageService.getPresignedGetUrl(
                "files",
                "test.txt",
                Duration.ofMinutes(5)
        );

        HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();

        try (InputStream in = connection.getInputStream()) {
            String content = new String(in.readAllBytes());
            assertThat(content).isEqualTo("hello");
        }
    }
}

