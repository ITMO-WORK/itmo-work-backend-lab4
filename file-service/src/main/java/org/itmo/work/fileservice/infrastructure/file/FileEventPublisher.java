package org.itmo.work.fileservice.infrastructure.file;

import org.itmo.work.fileservice.infrastructure.dto.events.FileUploadEvent;
import reactor.core.publisher.Mono;

public interface FileEventPublisher {

    void publishFileUploadedEvent(FileUploadEvent fileUploadEvent);
}
