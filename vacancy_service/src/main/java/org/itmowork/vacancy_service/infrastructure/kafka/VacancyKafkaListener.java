package org.itmowork.vacancy_service.infrastructure.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.itmowork.vacancy_service.dto.kafka.*;
import org.itmowork.vacancy_service.exception.exceptions.VacancyNotFoundException;
import org.itmowork.vacancy_service.service.interfaces.VacancyService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyKafkaListener {

    private final VacancyService vacancyService;
    private final VacancyKafkaProducer producer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "vacancy.request", groupId = "${spring.kafka.consumer.group-id}")
    public void onRequest(
            @Header(KafkaHeaders.RECEIVED_KEY) String vacancyIdKey,
            VacancyRequest request
    ) {
        UUID vacancyIdFromKey;
        try {
            vacancyIdFromKey = UUID.fromString(vacancyIdKey);
        } catch (Exception e) {
            sendError(vacancyIdKey, null, null, "BAD_KEY", "Kafka message key must be UUID vacancyId");
            return;
        }

        final String opRaw = request.type();
        final VacancyOperations op;
        try {
            op = VacancyOperations.valueOf(opRaw);
        } catch (Exception e) {
            sendError(vacancyIdKey, request, null, "UNKNOWN_OPERATION", "Unknown operation: " + opRaw);
            return;
        }

        try {
            switch (op) {
                case VACANCY_IS_PUBLISHED -> handleIsPublished(vacancyIdKey, vacancyIdFromKey, request, op);
                case VACANCY_IS_EXISTS -> handleExists(vacancyIdKey, vacancyIdFromKey, request, op);
                default -> sendError(vacancyIdKey, request, op, "UNKNOWN_OPERATION", "Unsupported operation: " + opRaw);
            }
        } catch (Exception e) {
            log.error("Unexpected error while handling request: {}", request, e);
            sendError(vacancyIdKey, request, op, "INTERNAL_ERROR", "Unexpected error");
        }
    }

    private void handleExists(
            String vacancyIdKey,
            UUID vacancyIdFromKey,
            VacancyRequest request,
            VacancyOperations op
    ) {
        final IsPublishedOrExistsPayload payload;

        try {
            payload = objectMapper.treeToValue(request.payload(), IsPublishedOrExistsPayload.class);
        } catch (JsonProcessingException e) {
            sendError(vacancyIdKey, request, op,
                    "BAD_REQUEST",
                    "Invalid payload format: " + e.getOriginalMessage());
            return;
        }

        UUID vacancyIdFromPayload = payload.vacancyId();
        if (vacancyIdFromPayload == null) {
            sendError(vacancyIdKey, request, op,
                    "BAD_REQUEST",
                    "payload.vacancy_id is required");
            return;
        }

        if (!vacancyIdFromPayload.equals(vacancyIdFromKey)) {
            sendError(vacancyIdKey, request, op,
                    "BAD_REQUEST",
                    "payload.vacancy_id must match message key");
            return;
        }

        try {
            boolean exists = vacancyService.existsVacancyById(vacancyIdFromKey);

            JsonNode resultNode = objectMapper.valueToTree(
                    new IsPublishedOrExistsResult(vacancyIdFromKey, exists)
            );

            producer.sendResponse(vacancyIdKey, new VacancyResponse(
                    op,
                    request.correlationId(),
                    true,
                    resultNode,
                    null
            ));
        } catch (Exception e) {
            log.error("Error in handleExists, vacancyId={}", vacancyIdFromKey, e);
            sendError(vacancyIdKey, request, op,
                    "INTERNAL_ERROR",
                    "Unexpected error");
        }
    }

    private void handleIsPublished(
            String vacancyIdKey,
            UUID vacancyIdFromKey,
            VacancyRequest request,
            VacancyOperations op
    ) {
        final IsPublishedOrExistsPayload payload;
        try {
            payload = objectMapper.treeToValue(request.payload(), IsPublishedOrExistsPayload.class);
        } catch (JsonProcessingException e) {
            sendError(vacancyIdKey, request, op, "BAD_REQUEST", "Invalid payload format: " + e.getOriginalMessage());
            return;
        }

        UUID vacancyIdFromPayload = payload.vacancyId();
        if (vacancyIdFromPayload == null) {
            sendError(vacancyIdKey, request, op, "BAD_REQUEST", "payload.vacancy_id is required");
            return;
        }

        if (!vacancyIdFromPayload.equals(vacancyIdFromKey)) {
            sendError(vacancyIdKey, request, op, "BAD_REQUEST", "payload.vacancy_id must match message key");
            return;
        }

        try {
            boolean published = vacancyService.isVacancyPublished(vacancyIdFromKey);
            JsonNode resultNode = objectMapper.valueToTree(new IsPublishedOrExistsResult(vacancyIdFromKey, published));
            producer.sendResponse(vacancyIdKey, new VacancyResponse(op, request.correlationId(), true, resultNode, null));
        } catch (VacancyNotFoundException e) {
            sendError(vacancyIdKey, request, op, "NOT_FOUND", e.getMessage());
        }
    }

    private void sendError(String vacancyIdKey, VacancyRequest request, VacancyOperations opOrNull, String code, String message
    ) {
        producer.sendResponse(vacancyIdKey, new VacancyResponse(
                opOrNull,
                request != null ? request.correlationId() : null,
                false,
                null,
                new ErrorPayload(code, message)
        ));
    }
}
