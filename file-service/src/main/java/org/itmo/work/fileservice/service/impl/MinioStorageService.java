package org.itmo.work.fileservice.service.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.config.MinioProperties;
import org.itmo.work.fileservice.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;


    @Override
    public void put(String bucket, String objectKey, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectKey)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception ex){
            throw new RuntimeException("Minio put failed", ex);
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ex){
            throw new RuntimeException("Minio delete", ex);
        }
    }
}
