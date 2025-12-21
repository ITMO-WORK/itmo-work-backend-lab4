package org.itmowork.vacancy_service.domain.exception.exceptions;

public class VacancyNotFoundException extends RuntimeException {
    public VacancyNotFoundException() {
        super();
    }

    public VacancyNotFoundException(String message) {
        super(message);
    }
}

