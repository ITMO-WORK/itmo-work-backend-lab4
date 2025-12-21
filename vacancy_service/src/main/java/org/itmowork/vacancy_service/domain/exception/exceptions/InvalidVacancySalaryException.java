package org.itmowork.vacancy_service.domain.exception.exceptions;

public class InvalidVacancySalaryException extends RuntimeException {
    public InvalidVacancySalaryException(String message) {
        super(message);
    }
}
