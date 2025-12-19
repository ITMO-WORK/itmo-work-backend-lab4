package org.itmowork.vacancy_service.domain.exception.exceptions;

public class InvalidVacancyStatusChangeException extends RuntimeException {
    public InvalidVacancyStatusChangeException(String message) {
        super(message);
    }
}