package org.itmo.work.fileservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void put(String bucket, String objectKey, MultipartFile file);

    void delete(String bucket, String objectKey);
}
