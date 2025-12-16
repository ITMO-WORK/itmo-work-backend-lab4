package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ilestegor.applicationservice.exception.exceptions.VacancyNotFoundException;
import org.ilestegor.applicationservice.infrastructure.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.infrastructure.kafka.common.pending.PendingRequest;
import org.ilestegor.applicationservice.infrastructure.kafka.common.producer.KafkaProducer;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.client.interfaces.VacancyClient;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.VacancyOperations;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.request.VacancyIdPayload;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.request.VacancyRequest;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response.ErrorPayload;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response.VacancyExistsResponse;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response.VacancyPublishedResponse;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response.VacancyResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyKafkaClient implements VacancyClient {

    private final KafkaProducer kafkaProducer;
    private final PendingRequest pendingRequest;
    private final ObjectMapper objectMapper;
    private final KafkaProps kafkaProps;


    private Mono<VacancyResponse> sendAndAwait(VacancyOperations operations, JsonNode payload, String key){
        UUID correlationId = UUID.randomUUID();

        VacancyRequest vacancyRequest = VacancyRequest.builder().correlationId(correlationId).type(operations).payload(payload).build();

        String topic = kafkaProps.kafka().vacancy().requestTopic();
        Duration timeout = kafkaProps.requestTimeout();

        Mono<VacancyResponse> await = pendingRequest.registerRequest(correlationId, timeout);

        Mono<Void> send = kafkaProducer.send(topic, key, vacancyRequest);

        return send.then(await);
    }

    private Mono<VacancyResponse> sendAndAwait(VacancyOperations operations, UUID vacancyId){
        JsonNode payload = objectMapper.valueToTree(new VacancyIdPayload(vacancyId));
        return sendAndAwait(operations, payload, vacancyId.toString());
    }

    private <T> Mono<T> decodePayload(VacancyResponse vacancyResponse, Class<T> clazz){
        try{
            return Mono.just(objectMapper.treeToValue(vacancyResponse.payload(), clazz));
        }catch (JsonProcessingException ex){
            return Mono.error(new RuntimeException("Failed to decode reply payload: " + clazz.getSimpleName(), ex));
        }
    }

    private Mono<VacancyResponse> ensureOk(VacancyResponse response){
        if (response == null)
            return Mono.error(new RuntimeException("Response is null"));
        if (response.ok())
            return Mono.just(response);
        ErrorPayload errorPayload = response.errorPayload();
        if (errorPayload == null)
            return Mono.error(new RuntimeException("Error Payload is null"));
        log.error("ERROR IN ENSURE OK");
        return Mono.error(new VacancyNotFoundException());
    }

    @Override
    public Mono<Boolean> isPublished(UUID vacancyId) {
        return sendAndAwait(VacancyOperations.VACANCY_IS_PUBLISHED, vacancyId)
                .flatMap(this::ensureOk)
                .flatMap(resp -> decodePayload(resp, VacancyPublishedResponse.class))
                .map(VacancyPublishedResponse::published);
    }

    @Override
    public Mono<Boolean> exists(UUID vacancyId) {
        return sendAndAwait(VacancyOperations.VACANCY_IS_EXISTS, vacancyId)
                .flatMap(this::ensureOk)
                .flatMap(resp -> decodePayload(resp, VacancyExistsResponse.class))
                .map(VacancyExistsResponse::vacancyExists);
    }
}
