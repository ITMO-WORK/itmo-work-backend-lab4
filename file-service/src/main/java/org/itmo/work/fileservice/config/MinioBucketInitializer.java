package org.itmo.work.fileservice.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void ensureBucket(){
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.bucket()).build()
            );
            if (!exists){
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.bucket()).build());
            }
        } catch (Exception ex){
            throw new IllegalStateException("Failed to ensure MinIO bucket : " + minioProperties.bucket(), ex);
        }
    }
}
