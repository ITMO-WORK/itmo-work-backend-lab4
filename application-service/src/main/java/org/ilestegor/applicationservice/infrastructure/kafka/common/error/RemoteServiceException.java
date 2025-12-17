package org.ilestegor.applicationservice.infrastructure.kafka.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RemoteServiceException extends RuntimeException{
    private final String code;

    public RemoteServiceException(String message, String code) {
        super(message);
        this.code = code;
    }
}
