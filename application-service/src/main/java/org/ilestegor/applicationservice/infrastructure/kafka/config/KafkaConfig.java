package org.ilestegor.applicationservice.infrastructure.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProps kafkaProps;

    @Bean
    public NewTopic vacancyRequestsTopic() {
        return TopicBuilder.name(kafkaProps.topics().applicationsEvents())
                .partitions(1)
                .replicas(3)
                .build();
    }
}
