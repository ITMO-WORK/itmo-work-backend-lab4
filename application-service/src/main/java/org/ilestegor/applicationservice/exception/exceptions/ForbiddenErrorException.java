package org.ilestegor.applicationservice.exception.exceptions;

public class ForbiddenErrorException extends RuntimeException {

    public ForbiddenErrorException() {
    }

    public ForbiddenErrorException(String message) {
        super(message);
    }
}
