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
    kafkaListenerContainerFactory(
            ObjectMapper objectMapper,
            KafkaProperties kafkaProperties
    ) {

        var props = kafkaProperties.buildConsumerProperties();

        JsonDeserializer<EventMessage> valueDeserializer =
                new JsonDeserializer<>(EventMessage.class, objectMapper);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setRemoveTypeHeaders(false);
        valueDeserializer.setUseTypeMapperForKey(false);

        DefaultKafkaConsumerFactory<String, EventMessage> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        props,
                        new StringDeserializer(),
                        valueDeserializer
                );

        ConcurrentKafkaListenerContainerFactory<String, EventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
