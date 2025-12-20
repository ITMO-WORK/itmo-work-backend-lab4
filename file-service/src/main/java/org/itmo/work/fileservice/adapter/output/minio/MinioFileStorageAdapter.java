package org.itmo.work.fileservice.adapter.output.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.itmo.work.fileservice.application.port.output.FileStoragePort;
import org.itmo.work.fileservice.application.port.output.FileStoragePropsPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Service
public class MinioFileStorageAdapter implements FileStoragePort {

    private final MinioClient internalClient;
    private final MinioClient presignClient;
    private final FileStoragePropsPort fileStoragePropsPort;

    public MinioFileStorageAdapter(
            @Qualifier("minioInternalClient") MinioClient internalClient,
            @Qualifier("minioPresignClient") MinioClient presignClient,
            FileStoragePropsPort fileStoragePropsPort
    ) {
        this.internalClient = internalClient;
        this.presignClient = presignClient;
        this.fileStoragePropsPort = fileStoragePropsPort;
    }

    @Override
    public void put(String bucket, String objectKey, MultipartFile file) {
        try {
            internalClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(fileStoragePropsPort.bucket())
                            .object(objectKey)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Minio put failed", ex);
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            internalClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(fileStoragePropsPort.bucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Minio delete failed", ex);
        }
    }

    @Override
    public String getPresignedGetUrl(String bucket, String objectKey, Duration expiry) {
        try {
            return presignClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) expiry.getSeconds())
                            .build()
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to generate presigned url for bucket=%s, key=%s".formatted(bucket, objectKey),
                    ex
            );
        }
    }
}
