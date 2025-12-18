package org.itmo.work.fileservice.service.impl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.config.MinioProperties;
import org.itmo.work.fileservice.model.StoredFile;
import org.itmo.work.fileservice.repository.StoredFileRepository;
import org.itmo.work.fileservice.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final StoredFileRepository storedFileRepository;


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

    @Override
    public String getPresignedGetUrl(String bucket, String objectKey, Duration expiry
    ) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) expiry.getSeconds())
                            .build()
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to generate presigned url for bucket=%s, key=%s"
                            .formatted(bucket, objectKey),
                    ex
            );
        }
    }


}
