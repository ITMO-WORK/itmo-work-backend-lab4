package org.ilestegor.applicationservice.dirty.exception.exceptions;

public class UserDoesNotBelongsToCompanyException extends RuntimeException{
    public UserDoesNotBelongsToCompanyException() {
    }

    public UserDoesNotBelongsToCompanyException(String message) {
        super(message);
    }
}
