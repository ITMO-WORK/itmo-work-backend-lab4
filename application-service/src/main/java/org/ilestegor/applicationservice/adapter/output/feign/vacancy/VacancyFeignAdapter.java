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
public class VacancyFeignAdapter implements VacancyPort {

    private final VacancyClient vacancyClient;

    @Override
    public Mono<Boolean> checkVacancyExists(UUID vacancyId, String token) {
        return Mono.fromCallable(() -> vacancyClient.isVacancyExists(vacancyId, token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(FeignException.NotFound.class, ex -> new VacancyNotFoundException())
                .map(Boolean.TRUE::equals);
    }

    @Override
    public Mono<Boolean> checkVacancyIsPublished(UUID vacancyId, String token) {
        return Mono.fromCallable(() -> vacancyClient.isVacancyPublished(vacancyId, token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(FeignException.NotFound.class, ex -> new VacancyNotFoundException())
                .map(Boolean.TRUE::equals);
    }

    @Override
    public Mono<String> getVacancyTitle(UUID vacancyId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        return Mono.fromCallable(() -> vacancyClient.getVacancyTitle(vacancyId, token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(FeignException.NotFound.class, ex -> new VacancyNotFoundException());
    }

    @Override
    public Mono<UUID> getCompanyIdByVacancyId(UUID vacancyId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        return Mono.fromCallable(() -> vacancyClient.getCompanyIdByVacancy(vacancyId, token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(FeignException.NotFound.class, ex -> new VacancyNotFoundException())
                .flatMap(companyId -> {
                    if (companyId == null) return Mono.error(new VacancyNotFoundException());
                    return Mono.just(companyId);
                });
    }
}
