package org.itmo.work.fileservice.adapter.output.kafka.listener;

import org.itmo.work.fileservice.adapter.output.kafka.dto.EventMessage;
import org.itmo.work.fileservice.adapter.output.kafka.dto.EventType;

public interface EventHandler {
    EventType support();
    void handle(EventMessage eventMessage);
}
