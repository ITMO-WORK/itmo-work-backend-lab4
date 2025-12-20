package com.itmowork.company_service.domain.model.exception.exceptions;

public class CompanyNotFoundException extends RuntimeException{

    public CompanyNotFoundException(String message){
        super(message);
    }
}
