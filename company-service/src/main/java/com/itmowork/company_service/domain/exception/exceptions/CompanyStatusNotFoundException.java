package com.itmowork.company_service.domain.exception.exceptions;

public class CompanyStatusNotFoundException extends RuntimeException{

    public CompanyStatusNotFoundException(String message){
        super(message);
    }
}
