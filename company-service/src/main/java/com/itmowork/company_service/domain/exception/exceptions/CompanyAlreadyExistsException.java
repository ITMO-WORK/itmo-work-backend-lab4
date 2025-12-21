package com.itmowork.company_service.domain.exception.exceptions;

public class CompanyAlreadyExistsException extends RuntimeException{

    public CompanyAlreadyExistsException(String message){
        super(message);
    }
}
