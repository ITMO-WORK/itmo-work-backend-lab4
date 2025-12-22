package com.itmowork.company_service.adapter.out.kafka.common;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface KafkaProducer {
    Mono<Void> send(String topic, String key, Object value);

    Mono<Void> send(String topic, Object value, Map<String, String> headers);
    Mono<Void> send(String topic, String key, Object value, Map<String, String> headers);
    Mono<Void> send(String topic, Object value);
}