package com.itmowork.notification_service.adapter.out.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.EventMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventMessage>
    kafkaListenerContainerFactory(ObjectMapper objectMapper,
                                  KafkaProperties kafkaProperties) {

        var props = kafkaProperties.buildConsumerProperties();

        var valueDeserializer =
                new JsonDeserializer<>(EventMessage.class, objectMapper);
        valueDeserializer.addTrustedPackages("*");

        var consumerFactory = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, EventMessage>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
