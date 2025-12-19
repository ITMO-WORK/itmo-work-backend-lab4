package org.ilestegor.applicationservice.dirty.exception.exceptions;

public class InvalidApplicationStatusForApplicationUpdate extends RuntimeException{
    public InvalidApplicationStatusForApplicationUpdate() {
    }

    public InvalidApplicationStatusForApplicationUpdate(String message) {
        super(message);
    }
}
