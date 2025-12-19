package org.itmowork.vacancy_service.application.port.out;

import org.itmowork.vacancy_service.domain.model.Currency;

public interface CurrencyRepositoryPort {
    Currency findByIdOrThrow(Long currencyId);
}