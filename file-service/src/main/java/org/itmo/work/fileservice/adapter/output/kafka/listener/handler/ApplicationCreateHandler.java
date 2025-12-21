package org.itmo.work.fileservice.adapter.output.kafka.listener.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.adapter.output.kafka.dto.EventMessage;
import org.itmo.work.fileservice.adapter.output.kafka.dto.EventType;
import org.itmo.work.fileservice.adapter.output.kafka.dto.events.ApplicationCreateEventDto;
import org.itmo.work.fileservice.adapter.output.kafka.listener.EventHandler;
import org.itmo.work.fileservice.application.port.input.RegisterApplicationPort;
import org.itmo.work.fileservice.application.port.input.UploadResumePort;
import org.itmo.work.fileservice.domain.exception.IllegalJsonFormatException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationCreateHandler implements EventHandler {

    private final ObjectMapper objectMapper;
    private final RegisterApplicationPort registerApplicationPort;

    @Override
    public EventType support() {
        return EventType.APPLICATION_CREATE;
    }

    @Override
    public void handle(EventMessage eventMessage) {
        try {
            var payload = objectMapper.treeToValue(eventMessage.payload(), ApplicationCreateEventDto.class);
            registerApplicationPort.register(payload.applicationId(), payload.userId(), payload.createdAt());
        } catch (JsonProcessingException ex){
            throw new IllegalJsonFormatException();
        }

    }
}
