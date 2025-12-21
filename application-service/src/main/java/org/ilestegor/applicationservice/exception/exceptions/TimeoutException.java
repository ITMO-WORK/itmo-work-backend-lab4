package org.ilestegor.applicationservice.exception.exceptions;

public class TimeoutException extends RuntimeException {
    public TimeoutException() {
    }

    public TimeoutException(String message) {
        super(message);
    }
}
