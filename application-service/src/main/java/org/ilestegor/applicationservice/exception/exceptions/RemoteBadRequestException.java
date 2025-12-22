package org.ilestegor.applicationservice.exception.exceptions;

public class RemoteBadRequestException extends RuntimeException{
    public RemoteBadRequestException() {
    }

    public RemoteBadRequestException(String message) {
        super(message);
    }
}
