package org.itmo.work.fileservice.adapter.output.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(Topics topics) {

    public record Topics(String filesEvents) {
    }
}
