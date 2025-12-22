package com.itmowork.user_service.adapter.in.kafka.company.requestListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.user_service.adapter.in.kafka.company.requestListener.dto.RequestMessage;
import com.itmowork.user_service.adapter.out.kafka.common.ErrorPayload;
import com.itmowork.user_service.adapter.out.kafka.common.ResponseMessage;
import com.itmowork.user_service.adapter.out.kafka.common.SpringKafkaProducer;
import com.itmowork.user_service.adapter.out.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRequestListener {

    private final SpringKafkaProducer springKafkaProducer;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @KafkaListener(
            topics = "${app.kafka.topics.user-request}",
            groupId = "user-service",
            containerFactory = "applicationRequestKafkaListenerFactory"
    )
    // компания не сможет присылать key пользователю, так как пока еще ничего не создано
    public void onMessage(
            @Payload RequestMessage msg,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(name = "Authorization", required = false) String authorization
    ) {
        if (msg == null || msg.eventType() == null) {
            return;
        }

        switch (msg.eventType()) {
            case USER_CREATE_EVENT -> handleUserCreate(msg, authorization);
        }
    }


    private void handleUserCreate(RequestMessage msg, String authorization) {

    }

    // TODO сделать реактивным!!

    private Mono<Void> runWithSecurityContextOrReplyError(
            RequestMessage msg,
            String authorization,
            Mono<Void> action
    ) {
        return Mono.defer(() -> {
            String token = extractBearerToken(authorization);
            if (token == null) {
                return sendError(msg, "UNAUTHORIZED", "Authorization header is required");
            }

            Claims claims;
            try {
                claims = jwtService.getClaimsFromToken(token);
            } catch (Exception e) {
                return sendError(msg, "UNAUTHORIZED", "Invalid JWT");
            }

            String userId = claims.get("userId", String.class);
            String email = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);

            if (userId == null || email == null) {
                return sendError(msg, "UNAUTHORIZED", "JWT does not contain required claims");
            }

            List<SimpleGrantedAuthority> authorities =
                    roles == null
                            ? List.of()
                            : roles.stream().map(SimpleGrantedAuthority::new).toList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);

            return action
                    .contextWrite(ctx ->
                            ctx.put(
                                    org.springframework.security.core.context.ReactiveSecurityContextHolder.SECURITY_CONTEXT_KEY,
                                    new org.springframework.security.core.context.SecurityContextImpl(auth)
                            )
                    );
        });
    }


    // TODO роли для создания владельза компании только админ
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

    private Mono<Void> sendError(RequestMessage msg, String code, String message) {
        ResponseMessage response = new ResponseMessage(
                msg.correlationId(),
                msg.eventType(),
                false,
                null,
                new ErrorPayload(code, message)
        );
        return springKafkaProducer.send(msg.replyTo().getValue(), response);
    }
}
