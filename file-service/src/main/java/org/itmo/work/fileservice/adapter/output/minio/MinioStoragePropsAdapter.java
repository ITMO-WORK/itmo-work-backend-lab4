package org.itmo.work.fileservice.adapter.output.minio;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.adapter.output.minio.config.MinioProperties;
import org.itmo.work.fileservice.application.port.output.FileStoragePropsPort;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MinioStoragePropsAdapter implements FileStoragePropsPort {

    private final MinioProperties minioProperties;

    @Override
    public String bucket() {
        return minioProperties.bucket();
    }

    @Override
    public Duration presignExpiry() {
        return minioProperties.presignExpiry();
    }
}
