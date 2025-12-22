package org.itmowork.vacancy_service.adapter.out.kafka.company.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.out.kafka.company.CompanyRpcPendingRequests;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.payload.CompanyResultPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyResponsesListener {

    private final CompanyRpcPendingRequests pending;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "vacancy.response",
            groupId = "vacancy-service-company-rpc",
            containerFactory = "companyResponseListenerFactory"
    )
    public void onResponse(@Payload CompanyResponseMessage msg) {
        if (msg == null || msg.correlationId() == null) return;

        // игнорируем чужие ответы/unknown
        if (msg.eventType() == null || msg.eventType() == CompanyEventType.UNKNOWN) {
            return;
        }
        if (msg.eventType() != CompanyEventType.COMPANY_EXISTS
                && msg.eventType() != CompanyEventType.COMPANY_VALIDATE_OWNERSHIP) {
            return;
        }

        if (!msg.ok()) {
            // Для CompanyPort boolean-методов обычно достаточно "false" на любые ошибки
            pending.complete(msg.correlationId(), false);
            return;
        }

        try {
            CompanyResultPayload payload = objectMapper.treeToValue(msg.payload(), CompanyResultPayload.class);
            pending.complete(msg.correlationId(), payload != null && payload.result());
        } catch (Exception e) {
            pending.complete(msg.correlationId(), false);
        }
    }
}