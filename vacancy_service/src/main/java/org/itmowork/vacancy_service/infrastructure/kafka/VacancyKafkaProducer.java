package org.itmowork.vacancy_service.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.itmowork.vacancy_service.dto.kafka.EventMessage;
import org.itmowork.vacancy_service.dto.kafka.EventType;
import org.itmowork.vacancy_service.dto.kafka.VacancyStatusChangePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyKafkaProducer {

    private final KafkaTemplate<String, EventMessage> eventKafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.vacancy-events:vacancy.events}")
    private String topic;

    public void publishVacancyStatusChanged(
            UUID vacancyId,
            UUID companyId,
            UUID actorUserId,
            String oldStatus,
            String newStatus
    ) {
        var payload = new VacancyStatusChangePayload(
                vacancyId,
                companyId,
                actorUserId,
                oldStatus,
                newStatus
        );

        var event = new EventMessage(
                UUID.randomUUID(),
                EventType.VACANCY_STATUS_CHANGE,
                Instant.now(),
                objectMapper.valueToTree(payload)
        );

        sendAfterCommit(vacancyId.toString(), event);
    }

    private void sendAfterCommit(String key, EventMessage event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendNow(key, event);
                }
            });
        } else {
            sendNow(key, event);
        }
    }

    private void sendNow(String key, EventMessage event) {
        eventKafkaTemplate.send(topic, key, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send vacancy event. topic={}, key={}, eventType={}",
                                topic, key, event.eventType(), ex);
                    } else {
                        log.debug("Vacancy event sent. topic={}, key={}, partition={}, offset={}",
                                topic, key, res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
                    }
                });
    }
}