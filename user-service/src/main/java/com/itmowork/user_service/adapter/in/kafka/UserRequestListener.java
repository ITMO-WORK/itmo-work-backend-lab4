package com.itmowork.user_service.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.user_service.adapter.in.kafka.dto.ErrorPayload;
import com.itmowork.user_service.adapter.in.kafka.dto.RequestMessage;
import com.itmowork.user_service.adapter.in.kafka.dto.RequestType;
import com.itmowork.user_service.adapter.in.kafka.dto.ResponseMessage;
import com.itmowork.user_service.adapter.in.kafka.dto.payload.CreateOwnerPayload;
import com.itmowork.user_service.adapter.in.kafka.dto.payload.UserCreateResultPayload;
import com.itmowork.user_service.adapter.in.kafka.dto.payload.UserExistsResultPayload;
import com.itmowork.user_service.adapter.in.kafka.dto.payload.UserIdPayload;
import com.itmowork.user_service.adapter.out.kafka.common.SpringKafkaProducer;
import com.itmowork.user_service.adapter.out.security.jwt.JwtService;
import com.itmowork.user_service.application.dto.RegisterCompanyOwnerCommand;
import com.itmowork.user_service.application.dto.query.GetUserByIdQuery;
import com.itmowork.user_service.application.port.in.GetUserByIdUseCase;
import com.itmowork.user_service.application.port.in.RegisterCompanyOwnerUseCase;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRequestListener {

    private final SpringKafkaProducer producer;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    private final RegisterCompanyOwnerUseCase registerCompanyOwnerUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;

    @KafkaListener(
            topics = "${app.kafka.topics.user-request}",
            groupId = "user-service",
            containerFactory = "userRequestKafkaListenerFactory"
    )
    public void onMessage(
            @Payload RequestMessage msg,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(name = "Authorization", required = false) String authorization
    ) {
        if (msg == null || msg.eventType() == null || msg.eventType() == RequestType.UNKNOWN) {
            if (msg != null) {
                sendError(msg, "UNSUPPORTED_OPERATION", "Unsupported event_type: " + msg.eventType()).subscribe();
            }
            return;
        }

        log.info(msg + "");

        Mono<Void> pipeline = switch (msg.eventType()) {
            case USER_CREATE_EVENT -> handleUserCreate(msg, authorization);
            case USER_EXISTS_EVENT -> handleUserExists(msg, key);
            default -> sendError(msg, "UNSUPPORTED_OPERATION", "Unsupported event_type: " + msg.eventType());
        };

        pipeline.subscribe();
    }

    private Mono<Void> handleUserCreate(RequestMessage msg, String authorization) {
        return isAdminOrReplyError(msg, authorization)
                .flatMap(allowed -> {
                    if (!allowed) return Mono.empty();

                    CreateOwnerPayload payload = readPayload(msg.payload(), CreateOwnerPayload.class);
                    log.info(payload + "");
                    if (payload == null || isBlank(payload.ownerEmail()) || isBlank(payload.ownerPassword()) || isBlank(payload.ownerFullName())) {
                        return sendError(msg, "BAD_REQUEST", "owner_full_name, owner_email, owner_password are required");
                    }

                    RegisterCompanyOwnerCommand command =
                            new RegisterCompanyOwnerCommand(payload.ownerFullName(), payload.ownerPassword(), payload.ownerEmail());

                    return registerCompanyOwnerUseCase.registerCompanyOwner(command)
                            .flatMap(result -> {
                                ResponseMessage response = new ResponseMessage(
                                        msg.eventType(),
                                        msg.correlationId(),
                                        true,
                                        objectMapper.valueToTree(new UserCreateResultPayload(result.id())),
                                        null
                                );
                                return producer.send(msg.replyTo().getValue(), response);
                            })
                            .onErrorResume(e -> mapDomainErrorToResponse(msg, e));
                });
    }

    private Mono<Void> handleUserExists(RequestMessage msg, String key) {
        UserIdPayload payload = readPayload(msg.payload(), UserIdPayload.class);
        if (payload == null || payload.userId() == null) {
            return sendError(msg, "BAD_REQUEST", "payload.user_id is required");
        }

        if (key == null || key.isBlank()) {
            return sendError(msg, "BAD_REQUEST", "message key is required and must match payload.user_id");
        }

        UUID keyUuid;
        try {
            keyUuid = UUID.fromString(key);
        } catch (IllegalArgumentException ex) {
            return sendError(msg, "BAD_REQUEST", "message key must be a valid UUID");
        }

        if (!keyUuid.equals(payload.userId())) {
            return sendError(msg, "BAD_REQUEST", "Kafka message key does not match payload.user_id");
        }

        return getUserByIdUseCase.getById(new GetUserByIdQuery(payload.userId()))
                .flatMap(u -> {
                    ResponseMessage response = new ResponseMessage(
                            msg.eventType(),
                            msg.correlationId(),
                            true,
                            objectMapper.valueToTree(new UserExistsResultPayload(u.id(), u.fullName(), u.email())),
                            null
                    );
                    return producer.send(msg.replyTo().getValue(), response);
                })
                .onErrorResume(e -> mapDomainErrorToResponse(msg, e));
    }


    private Mono<Boolean> isAdminOrReplyError(RequestMessage msg, String authorization) {
        String token = extractBearerToken(authorization);
        if (token == null) {
            return sendError(msg, "UNAUTHORIZED", "Authorization header is required")
                    .thenReturn(false);
        }

        Claims claims;
        try {
            claims = jwtService.getClaimsFromToken(token);
        } catch (Exception e) {
            return sendError(msg, "UNAUTHORIZED", "Invalid JWT")
                    .thenReturn(false);
        }

        List<String> roles = claims.get("roles", List.class);
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");

        if (!isAdmin) {
            return sendError(msg, "FORBIDDEN", "Admin role required")
                    .thenReturn(false);
        }

        return Mono.just(true);
    }

    private Mono<Void> mapDomainErrorToResponse(RequestMessage msg, Throwable e) {
        String name = e.getClass().getSimpleName();

        if (name.equals("UserAlreadyExistsException")) {
            return sendError(msg, "BAD_REQUEST", "User already exists");
        }
        if (name.equals("UserNotFoundException")) {
            return sendError(msg, "BAD_REQUEST", e.getMessage());
        }

        return sendError(msg, "INTERNAL_ERROR", "Unexpected error");
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) return null;
        String prefix = "Bearer ";
        if (authorization.startsWith(prefix)) return authorization.substring(prefix.length()).trim();
        return authorization.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Mono<Void> sendError(RequestMessage msg, String code, String message) {
        ResponseMessage response = new ResponseMessage(
                msg.eventType(),
                msg.correlationId(),
                false,
                null,
                new ErrorPayload(code, message)
        );
        log.info(msg + "");
        String topic = msg.replyTo() == null ? "application.response" : msg.replyTo().getValue();
        return producer.send(topic, response);
    }

    private <T> T readPayload(JsonNode node, Class<T> clazz) {
        try {
            return node == null ? null : objectMapper.treeToValue(node, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
