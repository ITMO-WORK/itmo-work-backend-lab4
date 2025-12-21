package org.ilestegor.applicationservice.adapter.output.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(Topics topics, Duration timeout) {
    public record Topics(String applicationsEvents,
                         String vacancyRequest,
                         String fileEvents,
                         String applicationResponse) {
    }
}
