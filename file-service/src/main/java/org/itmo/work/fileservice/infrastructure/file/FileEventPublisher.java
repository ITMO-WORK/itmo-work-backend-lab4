package org.itmo.work.fileservice.infrastructure.file;

import org.itmo.work.fileservice.infrastructure.dto.events.FileUploadEvent;

public interface FileEventPublisher {

    void publishFileUploadedEvent(FileUploadEvent fileUploadEvent);
}
