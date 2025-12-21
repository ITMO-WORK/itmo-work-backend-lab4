package org.ilestegor.applicationservice.adapter.output.feign.company;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.application.port.output.CompanyPort;
import org.ilestegor.applicationservice.exception.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyFeignAdapter implements CompanyPort {
    private final CompanyClient companyClient;

    public Mono<Boolean> isUserBelongsToCompany(UUID companyId, UUID userId,  String token){
        return Mono.fromCallable(() -> companyClient.isUserBelongsToCompany(companyId, userId, token))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(FeignException.NotFound.class, ex -> new UserNotFoundException())
                .map(Boolean.TRUE::equals);
    }

}
