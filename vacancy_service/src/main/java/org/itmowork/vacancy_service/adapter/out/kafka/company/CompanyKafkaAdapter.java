package org.itmowork.vacancy_service.adapter.out.kafka.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import java.nio.charset.StandardCharsets;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyEventType;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyRequestMessage;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.payload.CompanyExistsPayload;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.payload.CompanyValidateOwnershipPayload;
import org.itmowork.vacancy_service.adapter.out.kafka.company.exceptions.CompanyRpcException;
import org.itmowork.vacancy_service.adapter.out.security.SecurityUtils;
import org.itmowork.vacancy_service.application.port.out.CompanyPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class CompanyKafkaAdapter implements CompanyPort {

    private static final String REQUEST_TOPIC = "company.request";
    private static final String REPLY_TOPIC = "vacancy.response";
    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final KafkaTemplate<String, CompanyRequestMessage> companyRequestKafkaTemplate;
    private final CompanyRpcPendingRequests pending;
    private final ObjectMapper objectMapper;

    @Override
    public boolean exists(UUID companyId) {
        UUID correlationId = UUID.randomUUID();
        CompletableFuture<Boolean> future = pending.register(correlationId);

        CompanyRequestMessage req = new CompanyRequestMessage(
                CompanyEventType.COMPANY_EXISTS,
                correlationId,
                REPLY_TOPIC,
                objectMapper.valueToTree(new CompanyExistsPayload(companyId))
        );

        sendWithAuthHeader(REQUEST_TOPIC, companyId.toString(), req);

        return awaitOrFalse(correlationId, future);
    }

    @Override
    public boolean isOwnedBy(UUID companyId, UUID userId) {
        UUID correlationId = UUID.randomUUID();
        CompletableFuture<Boolean> future = pending.register(correlationId);

        CompanyRequestMessage req = new CompanyRequestMessage(
                CompanyEventType.COMPANY_VALIDATE_OWNERSHIP,
                correlationId,
                REPLY_TOPIC,
                objectMapper.valueToTree(new CompanyValidateOwnershipPayload(companyId, userId))
        );

        sendWithAuthHeader(REQUEST_TOPIC, companyId.toString(), req);

        return awaitOrFalse(correlationId, future);
    }

    private void sendWithAuthHeader(String topic, String key, CompanyRequestMessage message) {
        ProducerRecord<String, CompanyRequestMessage> record =
                new ProducerRecord<>(topic, key, message);

        String token = SecurityUtils.getCurrentToken();
        if (token != null && !token.isBlank()) {
            String headerValue = token.startsWith("Bearer ") ? token : "Bearer " + token;

            record.headers().add(new RecordHeader(
                    "Authorization",
                    headerValue.getBytes(StandardCharsets.UTF_8)
            ));
        }

        companyRequestKafkaTemplate.send(record);
    }

    private boolean awaitOrFalse(UUID correlationId, CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            pending.remove(correlationId);
            Throwable cause = ex.getCause();

            if (cause instanceof CompanyRpcException cre) {
                throw cre;
            }

            return false;
        } catch (TimeoutException ex) {
            pending.remove(correlationId);
            throw new CompanyRpcException("INTERNAL_ERROR", "Timeout waiting company-service response");
        } catch (Exception ex) {
            pending.remove(correlationId);
            return false;
        }
    }
}