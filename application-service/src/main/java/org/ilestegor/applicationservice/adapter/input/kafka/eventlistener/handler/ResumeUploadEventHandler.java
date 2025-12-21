package org.ilestegor.applicationservice.adapter.input.kafka.eventlistener.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ilestegor.applicationservice.adapter.input.kafka.eventlistener.EventHandler;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventType;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ResumeUploadedEventDto;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.exception.exceptions.ApplicationNotFoundException;
import org.ilestegor.applicationservice.exception.exceptions.IllegalJsonFormatException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeUploadEventHandler implements EventHandler {
    private final ObjectMapper objectMapper;
    private final ApplicationRepositoryPort applicationRepositoryPort;

    @Override
    public EventType support() {
        return EventType.RESUME_UPLOAD_EVENT;
    }

    @Override
    public void handle(EventMessage eventMessage) {
        try {
            var payload = objectMapper.treeToValue(eventMessage.payload(), ResumeUploadedEventDto.class);
            applicationRepositoryPort.findApplicationIdByUserId(payload.userId())
                    .switchIfEmpty(Mono.error(new ApplicationNotFoundException()))
                    .flatMap(appId -> applicationRepositoryPort.updateFileIdByApplicationId(appId, payload.fileId()))
                    .doOnError(e -> log.error("Failed to handle RESUME_UPLOAD_EVENT: {}", payload, e))
                    .subscribe();

        } catch (JsonProcessingException ex) {
            throw new IllegalJsonFormatException();
        }
    }
}
