package com.itmowork.company_service.mapper;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyUpdateRequestDto;
import com.itmowork.company_service.domain.model.Company;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompanyFromDto(@MappingTarget Company company, CompanyUpdateRequestDto companyRequestDto);
}
