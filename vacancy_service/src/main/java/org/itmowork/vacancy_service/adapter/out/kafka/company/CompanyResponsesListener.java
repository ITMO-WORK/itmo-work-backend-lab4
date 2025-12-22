package org.itmowork.vacancy_service.adapter.out.kafka.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.in.kafka.dto.ErrorPayload;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyEventType;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyResponseMessage;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.payload.CompanyResultPayload;
import org.itmowork.vacancy_service.adapter.out.kafka.company.exceptions.CompanyRpcException;
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

        if (msg.eventType() == null || msg.eventType() == CompanyEventType.UNKNOWN) return;

        if (msg.eventType() != CompanyEventType.COMPANY_EXISTS
                && msg.eventType() != CompanyEventType.COMPANY_VALIDATE_OWNERSHIP) {
            return;
        }

        if (!msg.ok()) {
            ErrorPayload err = msg.errorPayload();
            String code = err != null ? err.code() : "INTERNAL_ERROR";
            String message = err != null ? err.message() : "Company-service returned ok=false";

            pending.fail(msg.correlationId(), new CompanyRpcException(code, message));
            return;
        }

        try {
            CompanyResultPayload payload = objectMapper.treeToValue(msg.payload(), CompanyResultPayload.class);
            pending.complete(msg.correlationId(), payload != null && payload.result());
        } catch (Exception e) {
            pending.fail(msg.correlationId(), new CompanyRpcException("INTERNAL_ERROR", "Invalid response payload"));
        }
    }
}