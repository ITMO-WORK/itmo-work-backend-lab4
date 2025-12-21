package org.itmowork.vacancy_service.adapter.out.persistence;


import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.adapter.out.persistence.repository.CurrencyRepository;
import org.itmowork.vacancy_service.application.port.out.CurrencyRepositoryPort;
import org.itmowork.vacancy_service.domain.exception.exceptions.CurrencyNotFoundException;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencyJpaAdapter implements CurrencyRepositoryPort {

    private final CurrencyRepository currencyRepository;

    @Override
    public Currency findByIdOrThrow(Long currencyId) {
        return currencyRepository.findById(currencyId)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency with id " + currencyId + " not found"));
    }
}