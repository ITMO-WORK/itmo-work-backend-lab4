package org.ilestegor.applicationservice.adapter.input.kafka.eventlistener;


import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventType;

public interface EventHandler {
    EventType support();
    void handle(EventMessage eventMessage);
}
