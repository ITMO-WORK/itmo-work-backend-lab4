package org.ilestegor.applicationservice.dirty.exception.exceptions;

public class UserApplicationNotFoundException extends RuntimeException{
    public UserApplicationNotFoundException() {
    }

    public UserApplicationNotFoundException(String message) {
        super(message);
    }
}
