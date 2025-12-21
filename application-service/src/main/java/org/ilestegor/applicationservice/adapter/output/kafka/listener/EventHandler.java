package org.ilestegor.applicationservice.adapter.output.kafka.listener;


import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventType;

public interface EventHandler {
    EventType support();
    void handle(EventMessage eventMessage);
}
