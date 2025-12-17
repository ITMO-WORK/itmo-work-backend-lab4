package org.ilestegor.applicationservice.infrastructure.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(Topics topics)
{
    public record Topics(String applicationsEvents) {}
}
