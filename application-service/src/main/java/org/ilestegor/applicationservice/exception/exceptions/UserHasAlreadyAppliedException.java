package org.ilestegor.applicationservice.dirty.exception.exceptions;

public class UserHasAlreadyAppliedException extends RuntimeException{
    public UserHasAlreadyAppliedException() {
    }

    public UserHasAlreadyAppliedException(String message) {
        super(message);
    }
}
