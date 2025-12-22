package com.itmowork.company_service.domain.exception.exceptions;

public class UserInternalError extends RuntimeException {
    public UserInternalError(String message) {
        super(message);
    }
}
