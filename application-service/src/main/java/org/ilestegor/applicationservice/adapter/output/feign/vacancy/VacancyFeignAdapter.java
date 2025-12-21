package org.ilestegor.applicationservice.adapter.output.feign.vacancy;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;

import org.ilestegor.applicationservice.exception.exceptions.VacancyNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacancyFeignAdapter {

    private final VacancyClient vacancyClient;


}
