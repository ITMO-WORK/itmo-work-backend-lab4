package org.itmo.work.fileservice.application.port.input;

import java.util.UUID;

public interface GetResumeFilePort {

    String getDownloadUrl(UUID applicationId);
}
