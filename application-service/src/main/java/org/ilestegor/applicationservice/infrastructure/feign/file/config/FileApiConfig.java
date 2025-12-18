package org.ilestegor.applicationservice.infrastructure.feign.file.config;

import org.ilestegor.applicationservice.infrastructure.feign.file.FileClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FileApiConfig {

    @Bean
    @LoadBalanced
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public FileClient fileApi(WebClient.Builder builder) {
        WebClient wc = builder.baseUrl("lb://file-service").build();
        var factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(wc))
                .build();
        return factory.createClient(FileClient.class);
    }
}
