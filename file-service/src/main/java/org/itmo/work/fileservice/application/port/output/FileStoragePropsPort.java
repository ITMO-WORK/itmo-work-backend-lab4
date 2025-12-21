package org.itmo.work.fileservice.application.port.output;

import java.time.Duration;

public interface FileStoragePropsPort {
    String bucket();
    Duration presignExpiry();
}
