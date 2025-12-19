package org.itmowork.vacancy_service.application.port.out;


import java.util.UUID;

public interface CompanyPort {
    boolean exists(UUID companyId);
    boolean isOwnedBy(UUID companyId, UUID userId);
}