package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.error;

import org.ilestegor.applicationservice.exception.exceptions.VacancyNotFoundException;
import org.ilestegor.applicationservice.infrastructure.kafka.common.error.ErrorPayload;
import org.ilestegor.applicationservice.infrastructure.kafka.common.error.RemoteErrorMapper;
import org.springframework.stereotype.Component;

@Component
public class VacancyNotPublishedMapper implements RemoteErrorMapper {

    @Override
    public String code() {
        return "NOT_PUBLISHED";
    }

    @Override
    public RuntimeException toException(ErrorPayload errorPayload) {
        return new VacancyNotFoundException();
    }
}
