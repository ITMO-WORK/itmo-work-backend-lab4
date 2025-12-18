package org.itmo.work.fileservice.infrastructure.common.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, String key, Object message){
        kafkaTemplate.send(topic, key, message);
    }
}
