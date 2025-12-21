package org.ilestegor.applicationservice.adapter.output.kafka.common;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SpringKafkaProducer implements KafkaProducer{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> send(String topic, String key, Object message){
        return Mono.fromFuture(kafkaTemplate.send(topic, key, message)).then();
    }

    @Override
    public Mono<Void> send(String topic, String key, Object value, Map<String, String> headers) {

        return Mono.fromRunnable(() -> {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, value);
            headers.forEach((k, v) -> record.headers().add(k, v.getBytes(StandardCharsets.UTF_8)));
            kafkaTemplate.send(record);
        });
    }
}

