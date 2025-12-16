package org.ilestegor.applicationservice.infrastructure.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record KafkaProps(Kafka kafka, Duration requestTimeout)
{
    public record Kafka(Vacancy vacancy){}
    public record Vacancy (String requestTopic, String replyTopic){}
}
