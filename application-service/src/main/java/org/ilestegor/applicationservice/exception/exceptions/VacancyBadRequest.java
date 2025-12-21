package org.ilestegor.applicationservice.exception.exceptions;

public class VacancyBadRequest extends RuntimeException{
    public VacancyBadRequest() {
    }

    public VacancyBadRequest(String message) {
        super(message);
    }
}
