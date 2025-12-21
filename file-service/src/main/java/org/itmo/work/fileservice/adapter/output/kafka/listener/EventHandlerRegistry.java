package org.itmo.work.fileservice.adapter.output.kafka.listener;

import org.itmo.work.fileservice.adapter.output.kafka.dto.EventType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EventHandlerRegistry {

    private final Map<EventType, EventHandler> handlers;

    public EventHandlerRegistry(List<EventHandler> handlerList){
        this.handlers = handlerList.stream().collect(Collectors.toMap(EventHandler::support, h -> h));
    }

    public Optional<EventHandler> get(EventType eventType){
        return Optional.ofNullable(handlers.get(eventType));
    }
}
