package org.itmowork.vacancy_service.adapter.out.kafka.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyEventType;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyRequestMessage;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.payload.CompanyExistsPayload;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.payload.CompanyValidateOwnershipPayload;
import org.itmowork.vacancy_service.application.port.out.CompanyPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

        companyRequestKafkaTemplate.send(REQUEST_TOPIC, companyId.toString(), req);

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

        companyRequestKafkaTemplate.send(REQUEST_TOPIC, companyId.toString(), req);

        return awaitOrFalse(correlationId, future);
    }

    private boolean awaitOrFalse(UUID correlationId, CompletableFuture<Boolean> future) {
        try {
            return future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            pending.remove(correlationId);
            return false;
        }
    }
}