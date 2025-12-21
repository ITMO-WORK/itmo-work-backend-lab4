package org.ilestegor.applicationservice.adapter.input.kafka.eventlistener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventType;
import org.ilestegor.applicationservice.exception.exceptions.IllegalJsonFormatException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventListener {

    private final ObjectMapper objectMapper;
    private final EventHandlerRegistry eventHandlerRegistry;

    @KafkaListener(
            topics = "${app.kafka.topics.file-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String rawMessage) {
        EventMessage eventMessage;
        try {
            eventMessage = objectMapper.readValue(rawMessage, EventMessage.class);
        } catch (JsonProcessingException ex) {
            log.warn("Invalid json in kafka message {} ", rawMessage, ex);
            throw new IllegalJsonFormatException();
        }

        EventType eventType = eventMessage.eventType();
        var handlerOpt = eventHandlerRegistry.get(eventType);
        if (handlerOpt.isEmpty()) {
            log.debug("No handler for event type={}", eventType);
            return;
        }

        handlerOpt.get().handle(eventMessage);
    }
}
