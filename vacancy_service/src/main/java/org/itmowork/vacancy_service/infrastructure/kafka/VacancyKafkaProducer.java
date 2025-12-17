package org.itmowork.vacancy_service.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.dto.kafka.VacancyResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacancyKafkaProducer {

    private final KafkaTemplate<String, VacancyResponse> kafkaTemplate;

    public void sendResponse(String vacancyIdKey, VacancyResponse response) {
        kafkaTemplate.send("vacancy.response", vacancyIdKey, response);
    }
}