package org.ilestegor.applicationservice.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic vacancyRequestsTopic() {
        return TopicBuilder.name("vacancy.request")
                .partitions(1)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic vacancyResponseTopic() {
        return TopicBuilder.name("vacancy.response")
                .partitions(1)
                .replicas(3)
                .build();
    }
}
