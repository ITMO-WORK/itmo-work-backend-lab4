package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.consumer;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.infrastructure.kafka.common.pending.PendingRequest;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response.VacancyResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacancyKafkaConsumer {

    private final PendingRequest pendingRequest;

    @KafkaListener(topics = "${app.kafka.vacancy.reply-topic}")
    public void onMessage(VacancyResponse vacancyResponse){
        pendingRequest.completeRequest(vacancyResponse);;
    }

}
