package org.itmo.work.fileservice.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties){
        return MinioClient.builder().endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }
}
