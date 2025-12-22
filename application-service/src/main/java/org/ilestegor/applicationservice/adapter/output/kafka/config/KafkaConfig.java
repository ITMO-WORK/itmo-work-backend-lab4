package org.ilestegor.applicationservice.adapter.output.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ilestegor.applicationservice.adapter.input.kafka.common.ResponseMessage;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProps kafkaProps;

    @Bean
    public NewTopic applicationsEventsTopic() {
        return TopicBuilder.name(kafkaProps.topics().applicationsEvents())
                .partitions(1)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic applicationResponseTopic() {
        return TopicBuilder.name(kafkaProps.topics().applicationResponse())
                .partitions(1)
                .replicas(3)
                .build();
    }


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
    public ConcurrentKafkaListenerContainerFactory<String, ResponseMessage>
    responseMessageKafkaListenerContainerFactory(
            ConsumerFactory<String, ResponseMessage> responseMessageConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ResponseMessage>();
        factory.setConsumerFactory(responseMessageConsumerFactory);
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
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
