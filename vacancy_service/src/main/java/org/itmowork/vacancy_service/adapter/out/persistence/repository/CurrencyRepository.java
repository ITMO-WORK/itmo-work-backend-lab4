package org.itmowork.vacancy_service.adapter.out.persistence.repository;

import org.itmowork.vacancy_service.domain.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findById(Long currencyId);
}
