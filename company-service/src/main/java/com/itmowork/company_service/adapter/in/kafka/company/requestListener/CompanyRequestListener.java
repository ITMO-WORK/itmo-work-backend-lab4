package com.itmowork.company_service.adapter.in.kafka.company.requestListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.company_service.adapter.in.kafka.company.requestListener.dto.CompanyValidateOwnershipPayloadRequest;
import com.itmowork.company_service.adapter.in.kafka.company.requestListener.dto.CompanyValidateOwnershipPayloadResponse;
import com.itmowork.company_service.adapter.in.kafka.company.requestListener.dto.ExistsCompanyByIdPayloadRequest;
import com.itmowork.company_service.adapter.in.kafka.company.requestListener.dto.ExistsCompanyByIdPayloadResponse;
import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.ResponseMessage;
import com.itmowork.company_service.adapter.out.kafka.common.*;
import com.itmowork.company_service.application.usecase.ExistsCompanyByIdUseCase;
import com.itmowork.company_service.application.usecase.ValidateCompanyOwnershipUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyRequestListener {

    private final SpringKafkaProducer springKafkaProducer;
    private final ObjectMapper objectMapper;
    private final ValidateCompanyOwnershipUseCase validateCompanyOwnershipUseCase;
    private final ExistsCompanyByIdUseCase existsCompanyByIdUseCase;

    @KafkaListener(
            topics = "${app.kafka.topics.company-request}",
            groupId = "company-service",
            containerFactory = "requestMessageKafkaListenerContainerFactory"
    )
    public void onMessage(
            @Payload RequestMessage msg,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(name = "Authorization", required = false) String authorization
    )
    {
        if (msg == null || msg.eventType() == null || msg.eventType() == RequestType.UNKNOWN) {
            if (msg != null) {
                sendError(msg, ErrorCode.UNSUPPORTED_OPERATION, "Unsupported event_type: " + msg.eventType()).subscribe();
            }
            return;
        }
        Mono<Void> pipeline = switch (msg.eventType()) {
            case COMPANY_VALIDATE_OWNERSHIP -> handleCompanyValidateOwnership(msg, key);
            case COMPANY_EXISTS ->  handleCompanyExists(msg, key);
            default -> sendError(msg, ErrorCode.UNSUPPORTED_OPERATION, "Unsupported event_type: " + msg.eventType());
        };

        pipeline.subscribe();

    }

    Mono<Void> handleCompanyValidateOwnership(RequestMessage msg, String key) {
        CompanyValidateOwnershipPayloadRequest requestPayload = readPayload(msg.payload(), CompanyValidateOwnershipPayloadRequest.class);
        if(requestPayload == null){
            return sendError(msg, ErrorCode.BAD_REQUEST, "Invalid request payload");
        }
        UUID companyId = requestPayload.companyId();
        UUID userId = requestPayload.userId();

        if(companyId == null || userId == null){
            return sendError(msg, ErrorCode.BAD_REQUEST, "companyId and userId must be provided");
        }

        Mono<Boolean> resultMono = validateCompanyOwnershipUseCase.validateCompanyOwnership(companyId, userId);
       return resultMono.flatMap(isOwner -> {
            JsonNode responsePayload = objectMapper.valueToTree(new CompanyValidateOwnershipPayloadResponse(companyId, isOwner));
            ResponseMessage responseMessage = ok(msg, responsePayload);
            String topic = msg.replyTo().getValue();
            return springKafkaProducer.send(topic, key, responseMessage);
        });
    }

    Mono<Void> handleCompanyExists(RequestMessage msg, String key){
        ExistsCompanyByIdPayloadRequest requestPayload = readPayload(msg.payload(), ExistsCompanyByIdPayloadRequest.class);
        if(requestPayload == null){
            return sendError(msg, ErrorCode.BAD_REQUEST, "Invalid request payload");
        }
        UUID companyId = requestPayload.companyId();
        if(companyId == null){
            return sendError(msg, ErrorCode.BAD_REQUEST, "companyId must be provided");
        }
        Mono<Boolean> resultMono = existsCompanyByIdUseCase.existsCompanyById(companyId);
        return resultMono.flatMap(exists -> {
            JsonNode responsePayload = objectMapper.valueToTree(new ExistsCompanyByIdPayloadResponse(companyId, exists));
            ResponseMessage responseMessage = ok(msg, responsePayload);
            String topic = msg.replyTo().getValue();
            return springKafkaProducer.send(topic, key, responseMessage);
        });
    }

    private ResponseMessage ok(RequestMessage msg, JsonNode payload) {
        return new ResponseMessage(
                msg.correlationId(),
                msg.eventType(),
                true,
                payload,
                null
        );
    }

    private Mono<Void> sendError(RequestMessage msg, ErrorCode code, String message) {
        ResponseMessage response = new ResponseMessage(
                msg.correlationId(),
                msg.eventType(),
                false,
                null,
                objectMapper.valueToTree(new ErrorPayload(code, message)
                ));

        String topic = msg.replyTo().getValue();
        return springKafkaProducer.send(topic, response);
    }

    private <T> T readPayload(JsonNode node, Class<T> clazz) {
        try {
            return node == null ? null : objectMapper.treeToValue(node, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
