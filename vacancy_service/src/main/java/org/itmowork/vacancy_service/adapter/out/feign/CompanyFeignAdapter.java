//package org.itmowork.vacancy_service.adapter.out.feign;
//
//import lombok.RequiredArgsConstructor;
//import org.itmowork.vacancy_service.application.port.out.CompanyPort;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//public class CompanyFeignAdapter implements CompanyPort {
//
//    private final CompanyClient companyClient;
//
//    @Override
//    public boolean exists(UUID companyId) {
//        Boolean exists = companyClient.existsCompany(companyId);
//        return exists != null && exists;
//    }
//
//    @Override
//    public boolean isOwnedBy(UUID companyId, UUID userId) {
//        Boolean owns = companyClient.validateCompanyOwnership(companyId, userId);
//        return owns != null && owns;
//    }
//}