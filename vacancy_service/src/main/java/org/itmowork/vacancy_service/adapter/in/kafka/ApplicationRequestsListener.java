package org.itmowork.vacancy_service.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.in.kafka.dto.*;
import org.itmowork.vacancy_service.adapter.in.kafka.dto.payload.*;
import org.itmowork.vacancy_service.adapter.out.security.jwt.JwtService;
import org.itmowork.vacancy_service.application.port.in.VacancyQueriesUseCase;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ApplicationRequestsListener {

    private static final String DEFAULT_RESPONSE_TOPIC = "application.response";

    private final VacancyQueriesUseCase vacancyQueriesUseCase;
    private final KafkaTemplate<String, VacancyResponseMessage> responseKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @KafkaListener(
            topics = "vacancy.request",
            groupId = "vacancy-service",
            containerFactory = "applicationRequestKafkaListenerFactory"
    )
    public void onMessage(
            @Payload ApplicationRequestMessage msg,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(name = "Authorization", required = false) String authorization
    ) {
        if (msg == null) {
            return;
        }

        if (msg.eventType() == null || msg.eventType() == EventType.UNKNOWN) {
            sendError(msg, "UNSUPPORTED_OPERATION", "Unsupported event_type: " + msg.eventType());
            return;
        }

        switch (msg.eventType()) {
            case VACANCY_IS_PUBLISHED -> handleIsPublished(msg, key);
            case VACANCY_EXISTS -> handleExists(msg, key);
            case VACANCY_TITLE -> handleTitle(msg, key, authorization);
            case VACANCY_COMPANY_ID -> handleCompanyId(msg, key, authorization);
            default -> sendError(msg, "UNSUPPORTED_OPERATION", "Unsupported event_type: " + msg.eventType());
        }
    }

    private void handleIsPublished(ApplicationRequestMessage msg, String key) {
        VacancyIdPayload payload = requireVacancyId(msg, key);
        if (payload == null) return;

        try {
            boolean result = vacancyQueriesUseCase.isPublished(payload.vacancyId());

            VacancyResponseMessage response = ok(msg,
                    objectMapper.valueToTree(new VacancyIsPublishedResultPayload(payload.vacancyId(), result)));

            responseKafkaTemplate.send(resolveReplyTopic(msg), payload.vacancyId().toString(), response);

        } catch (VacancyNotFoundException ex) {
            sendError(msg, "BAD_REQUEST", "Given vacancy_id is not found");
        } catch (Exception ex) {
            sendError(msg, "INTERNAL_ERROR", "Unexpected error");
        }
    }

    private void handleExists(ApplicationRequestMessage msg, String key) {
        VacancyIdPayload payload = requireVacancyId(msg, key);
        if (payload == null) return;

        try {
            boolean result = vacancyQueriesUseCase.exists(payload.vacancyId());

            VacancyResponseMessage response = ok(msg,
                    objectMapper.valueToTree(new VacancyExistsResultPayload(payload.vacancyId(), result)));

            responseKafkaTemplate.send(resolveReplyTopic(msg), payload.vacancyId().toString(), response);

        } catch (Exception ex) {
            sendError(msg, "INTERNAL_ERROR", "Unexpected error");
        }
    }

    private void handleTitle(ApplicationRequestMessage msg, String key, String authorization) {
        VacancyIdPayload payload = requireVacancyId(msg, key);
        if (payload == null) return;

        runWithSecurityContextOrReplyError(msg, authorization, () -> {
            if (!hasAnyAllowedRole()) {
                sendError(msg, "FORBIDDEN", "Insufficient permissions");
                return;
            }

            try {
                String title = vacancyQueriesUseCase.getTitle(payload.vacancyId());

                VacancyResponseMessage response = ok(msg,
                        objectMapper.valueToTree(new VacancyTitleResultPayload(payload.vacancyId(), title)));

                responseKafkaTemplate.send(resolveReplyTopic(msg), payload.vacancyId().toString(), response);

            } catch (VacancyNotFoundException ex) {
                sendError(msg, "BAD_REQUEST", "Given vacancy_id is not found");
            } catch (Exception ex) {
                sendError(msg, "INTERNAL_ERROR", "Unexpected error");
            }
        });
    }

    private void handleCompanyId(ApplicationRequestMessage msg, String key, String authorization) {
        VacancyIdPayload payload = requireVacancyId(msg, key);
        if (payload == null) return;

        runWithSecurityContextOrReplyError(msg, authorization, () -> {
            if (!hasAnyAllowedRole()) {
                sendError(msg, "FORBIDDEN", "Insufficient permissions");
                return;
            }

            try {
                UUID companyId = vacancyQueriesUseCase.getCompanyId(payload.vacancyId());

                VacancyResponseMessage response = ok(msg,
                        objectMapper.valueToTree(new VacancyCompanyIdResultPayload(payload.vacancyId(), companyId)));

                responseKafkaTemplate.send(resolveReplyTopic(msg), payload.vacancyId().toString(), response);

            } catch (VacancyNotFoundException ex) {
                sendError(msg, "BAD_REQUEST", "Given vacancy_id is not found");
            } catch (Exception ex) {
                sendError(msg, "INTERNAL_ERROR", "Unexpected error");
            }
        });
    }


    private VacancyIdPayload requireVacancyId(ApplicationRequestMessage msg, String key) {
        VacancyIdPayload payload = readPayload(msg.payload(), VacancyIdPayload.class);
        if (payload == null || payload.vacancyId() == null) {
            sendError(msg, "BAD_REQUEST", "payload.vacancy_id is required");
            return null;
        }

        if (key == null || key.isBlank()) {
            sendError(msg, "BAD_REQUEST", "message key is required and must match payload.vacancy_id");
            return null;
        }

        UUID keyUuid;
        try {
            keyUuid = UUID.fromString(key);
        } catch (IllegalArgumentException ex) {
            sendError(msg, "BAD_REQUEST", "message key must be a valid UUID");
            return null;
        }

        if (!keyUuid.equals(payload.vacancyId())) {
            sendError(msg, "BAD_REQUEST", "Kafka message key does not match payload.vacancy_id");
            return null;
        }

        return payload;
    }

    private void runWithSecurityContextOrReplyError(ApplicationRequestMessage msg, String authorization, Runnable action) {
        String token = extractBearerToken(authorization);
        if (token == null) {
            sendError(msg, "UNAUTHORIZED", "Authorization header is required");
            return;
        }

        try {
            Claims claims = jwtService.parseAllClaims(token);

            String userId = claims.get("userId", String.class);
            String email = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);

            if (userId == null || email == null) {
                sendError(msg, "UNAUTHORIZED", "JWT does not contain required claims");
                return;
            }

            List<SimpleGrantedAuthority> authorities =
                    roles == null
                            ? List.of()
                            : roles.stream().map(SimpleGrantedAuthority::new).toList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);
            auth.setDetails(email);

            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            action.run();

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            sendError(msg, "UNAUTHORIZED", "Invalid JWT");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean hasAnyAllowedRole() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> {
            String r = a.getAuthority();
            return r.equals("ROLE_ADMIN")
                    || r.equals("ROLE_MANAGER")
                    || r.equals("ROLE_COMPANY_OWNER")
                    || r.equals("ROLE_EMPLOYEE");
        });
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) return null;
        String prefix = "Bearer ";
        if (authorization.startsWith(prefix)) return authorization.substring(prefix.length()).trim();
        return authorization.trim();
    }

    private VacancyResponseMessage ok(ApplicationRequestMessage msg, JsonNode payload) {
        return new VacancyResponseMessage(
                msg.eventType(),
                msg.correlationId(),
                true,
                payload,
                null
        );
    }

    private String resolveReplyTopic(ApplicationRequestMessage msg) {
        return DEFAULT_RESPONSE_TOPIC;
    }

    private void sendError(ApplicationRequestMessage msg, String code, String message) {
        VacancyResponseMessage response = new VacancyResponseMessage(
                msg.eventType(),
                msg.correlationId(),
                false,
                null,
                new ErrorPayload(code, message)
        );
        responseKafkaTemplate.send(resolveReplyTopic(msg), (String) null, response);
    }

    private <T> T readPayload(JsonNode node, Class<T> clazz) {
        try {
            return node == null ? null : objectMapper.treeToValue(node, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}

