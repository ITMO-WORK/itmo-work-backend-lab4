package org.ilestegor.applicationservice.adapter.input.kafka.vacancy.responselistener;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.vacancy.responselistener.dto.ResponseMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationResponseListener {

    private final ResponseDispatcher responseDispatcher;

    @KafkaListener(
            topics = "${app.kafka.topics.application-response}",
            containerFactory = "responseMessageKafkaListenerContainerFactory",
            groupId = "application-service"
    )
    public void onMessage(ResponseMessage responseMessage) {
        responseDispatcher.dispatch(responseMessage);
    }
}
