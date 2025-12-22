package com.itmowork.company_service.domain.exception.exceptions;

public class WrongEventException extends RuntimeException {
    public WrongEventException(String message) {
        super(message);
    }
}
