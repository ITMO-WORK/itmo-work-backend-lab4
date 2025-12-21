package org.ilestegor.applicationservice.exception.exceptions;

public class WrongEventException extends RuntimeException{
    public WrongEventException() {
    }

    public WrongEventException(String message) {
        super(message);
    }
}
