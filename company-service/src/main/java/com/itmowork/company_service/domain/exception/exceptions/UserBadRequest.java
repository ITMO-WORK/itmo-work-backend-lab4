package com.itmowork.company_service.domain.exception.exceptions;

public class UserBadRequest extends RuntimeException {
    public UserBadRequest(String message) {
        super(message);
    }
}
