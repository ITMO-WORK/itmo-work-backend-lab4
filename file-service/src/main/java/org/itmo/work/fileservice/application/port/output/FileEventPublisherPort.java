package org.itmo.work.fileservice.application.port.output;


import org.itmo.work.fileservice.adapter.output.kafka.dto.events.FileUploadEvent;

public interface FileEventPublisherPort {
    void publishFileUploadedEvent(FileUploadEvent fileUploadEvent);
}
