package org.ilestegor.applicationservice.adapter.output.kafka.common;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public Mono<Void> send(String topic, String key, Object message){
        return Mono.fromFuture(kafkaTemplate.send(topic, key, message)).then();
    }
}

