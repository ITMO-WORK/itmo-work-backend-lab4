package org.itmowork.vacancy_service.adapter.out.kafka.company.exceptions;

public class CompanyRpcException extends RuntimeException {
    private final String code;

    public CompanyRpcException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}