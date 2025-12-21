package org.ilestegor.applicationservice.adapter.output.kafka.common;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface KafkaProducer {
    Mono<Void> send(String topic, String key, Object value);

    Mono<Void> send(String topic, String key, Object value, Map<String, String> headers);
}
