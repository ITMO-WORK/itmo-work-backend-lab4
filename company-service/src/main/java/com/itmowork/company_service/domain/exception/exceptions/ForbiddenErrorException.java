package com.itmowork.company_service.domain.exception.exceptions;

public class ForbiddenErrorException extends RuntimeException {
    public ForbiddenErrorException(String message) {
        super(message);
    }
}
