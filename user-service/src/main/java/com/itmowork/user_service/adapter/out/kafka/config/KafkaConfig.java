package com.itmowork.user_service.adapter.out.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.user_service.adapter.out.kafka.common.ResponseMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper
    ) {
        var factory = new DefaultKafkaProducerFactory<String, Object>(
                kafkaProperties.buildProducerProperties()
        );

        var serializer = new JsonSerializer<>(objectMapper);
        serializer.setAddTypeInfo(false);

        factory.setValueSerializer(serializer);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ResponseMessage> responseMessageConsumerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper
    ) {
        var props = kafkaProperties.buildConsumerProperties();

        var valueDeserializer = new JsonDeserializer<>(ResponseMessage.class, objectMapper);
        valueDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }


    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
