package com.itmowork.user_service.adapter.out.kafka.common;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SpringKafkaProducer implements KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> send(String topic, String key, Object message) {
        return Mono.fromFuture(kafkaTemplate.send(topic, key, message)).then();
    }

    @Override
    public Mono<Void> send(String topic, Object message) {
        return Mono.fromFuture(kafkaTemplate.send(topic, message)).then();
    }

    @Override
    public Mono<Void> send(String topic, Object value, Map<String, String> headers) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, value);
        addHeaders(record, headers);
        CompletableFuture<?> future = kafkaTemplate.send(record);
        return Mono.fromFuture(future).then();
    }

    @Override
    public Mono<Void> send(String topic, String key, Object value, Map<String, String> headers) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, value);
        addHeaders(record, headers);
        CompletableFuture<?> future = kafkaTemplate.send(record);
        return Mono.fromFuture(future).then();
    }

    private void addHeaders(ProducerRecord<String, Object> record, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return;

        headers.forEach((k, v) -> {
            if (k == null || v == null) return;
            record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8)));
        });
    }
}
