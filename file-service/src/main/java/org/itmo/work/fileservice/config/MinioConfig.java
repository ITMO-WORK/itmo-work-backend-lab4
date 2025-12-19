package org.itmo.work.fileservice.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    @Qualifier("minioInternalClient")
    public MinioClient minioInternalClient(MinioProperties p) {
        return MinioClient.builder()
                .endpoint(p.internalEndpoint())
                .credentials(p.accessKey(), p.secretKey())
                .build();
    }

    @Bean
    @Qualifier("minioPresignClient")
    public MinioClient minioPresignClient(MinioProperties p) {
        return MinioClient.builder()
                .endpoint(p.publicEndpoint())
                .credentials(p.accessKey(), p.secretKey())
                .build();
    }
}
