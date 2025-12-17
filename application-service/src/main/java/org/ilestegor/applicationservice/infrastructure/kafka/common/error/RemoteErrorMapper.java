package org.ilestegor.applicationservice.infrastructure.kafka.common.error;

public interface RemoteErrorMapper {
    String code();
    RuntimeException toException(ErrorPayload errorPayload);
}
