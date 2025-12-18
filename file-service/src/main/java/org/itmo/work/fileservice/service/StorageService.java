package org.itmo.work.fileservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

public interface StorageService {
    void put(String bucket, String objectKey, MultipartFile file);

    void delete(String bucket, String objectKey);



    String getPresignedGetUrl(
            String bucket,
            String objectKey,
            Duration expiry
    );
}
