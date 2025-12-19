package org.itmo.work.fileservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String internalEndpoint,
        String publicEndpoint,
        String accessKey,
        String secretKey,
        String bucket,
        Duration presignExpiry
) {
}
