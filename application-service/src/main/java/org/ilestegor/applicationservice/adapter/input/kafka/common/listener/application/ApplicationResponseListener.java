package org.ilestegor.applicationservice.adapter.input.kafka.common.listener.application;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.common.ResponseMessage;
import org.ilestegor.applicationservice.adapter.input.kafka.common.dispatcher.ResponseDispatcher;
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
