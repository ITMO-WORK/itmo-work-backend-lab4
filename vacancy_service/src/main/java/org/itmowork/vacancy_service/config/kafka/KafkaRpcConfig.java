package org.itmowork.vacancy_service.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.itmowork.vacancy_service.adapter.in.kafka.dto.ApplicationRequestMessage;
import org.itmowork.vacancy_service.adapter.in.kafka.dto.VacancyResponseMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaRpcConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.application.name:vacancy-service}")
    private String appName;

    @Bean
    public ConsumerFactory<String, ApplicationRequestMessage> applicationRequestConsumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<ApplicationRequestMessage> valueDeserializer =
                new JsonDeserializer<>(ApplicationRequestMessage.class, objectMapper, false);

        valueDeserializer.addTrustedPackages("org.itmowork.vacancy_service.*");
        valueDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationRequestMessage> applicationRequestKafkaListenerFactory(
            ConsumerFactory<String, ApplicationRequestMessage> applicationRequestConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ApplicationRequestMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(applicationRequestConsumerFactory);
        return factory;
    }

    @Bean
    public ProducerFactory<String, VacancyResponseMessage> vacancyResponseProducerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, appName + "-rpc");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        DefaultKafkaProducerFactory<String, VacancyResponseMessage> factory =
                new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, VacancyResponseMessage> vacancyResponseKafkaTemplate(
            ProducerFactory<String, VacancyResponseMessage> vacancyResponseProducerFactory
    ) {
        return new KafkaTemplate<>(vacancyResponseProducerFactory);
    }
}
