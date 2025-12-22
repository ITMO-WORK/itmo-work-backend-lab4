package org.itmowork.vacancy_service.adapter.out.kafka.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.out.kafka.notification.dto.EventMessage;
import org.itmowork.vacancy_service.adapter.out.kafka.notification.dto.EventType;
import org.itmowork.vacancy_service.adapter.out.kafka.notification.dto.VacancyStatusChangePayload;
import org.itmowork.vacancy_service.application.dto.event.VacancyStatusChangedEvent;
import org.itmowork.vacancy_service.application.port.out.VacancyEventPublisherPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VacancyKafkaEventPublisherAdapter implements VacancyEventPublisherPort {

    private final KafkaTemplate<String, EventMessage> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String topic = "vacancies.events";

    @Override
    public void publishStatusChanged(VacancyStatusChangedEvent event) {
        VacancyStatusChangePayload payload = new VacancyStatusChangePayload(
                event.vacancyId(),
                event.companyId(),
                event.actorUserId(),
                event.oldStatus().name(),
                event.newStatus().name()
        );

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.VACANCY_STATUS_CHANGE,
                Instant.now(),
                objectMapper.valueToTree(payload)
        );

        kafkaTemplate.send(topic, event.vacancyId().toString(), message);
    }
}
