package org.itmo.work.fileservice.application.port.output;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface FileStoragePort {
    void put(String bucket, String objectKey, MultipartFile file);

    void delete(String bucket, String objectKey);

    String getPresignedGetUrl(String bucket, String objectKey, Duration expiry);
}
