package org.ilestegor.applicationservice.exception.exceptions;

public class RemoteServiceException extends RuntimeException{
    public RemoteServiceException() {
    }

    public RemoteServiceException(String message) {
        super(message);
    }
}
