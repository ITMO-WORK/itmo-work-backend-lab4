package org.itmowork.vacancy_service.domain.exception.exceptions;

public class InvalidVacancyStatusException extends RuntimeException {
    public InvalidVacancyStatusException(String message) {
        super(message);
    }
}