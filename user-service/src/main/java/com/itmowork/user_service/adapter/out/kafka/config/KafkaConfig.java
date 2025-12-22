package com.itmowork.user_service.adapter.out.kafka.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.user_service.adapter.in.kafka.dto.RequestMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
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
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ✅ consumer для RequestMessage (важно!)
    @Bean
    public ConsumerFactory<String, RequestMessage> requestMessageConsumerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        ObjectMapper kafkaMapper = objectMapper.copy()
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);

        JsonDeserializer<RequestMessage> valueDeserializer =
                new JsonDeserializer<>(RequestMessage.class, kafkaMapper, false);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean(name = "userRequestKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, RequestMessage> userRequestKafkaListenerFactory(
            ConsumerFactory<String, RequestMessage> requestMessageConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RequestMessage>();
        factory.setConsumerFactory(requestMessageConsumerFactory);
        return factory;
    }
}
