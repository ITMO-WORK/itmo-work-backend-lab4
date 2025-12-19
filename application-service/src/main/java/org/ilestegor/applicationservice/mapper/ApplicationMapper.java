package org.ilestegor.applicationservice.dirty.mapper;

import org.ilestegor.applicationservice.dirty.dto.ApplicationDto;
import org.ilestegor.applicationservice.dirty.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.dirty.model.Application;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Application application, ApplicationCreateRequestDto applicationCreateRequestDto);

    @Mapping(target = "vacancyId", source = "vacancyId")
    @Mapping(target = "userId", source = "userId")
    ApplicationDto fromApplicationtoApplicationDto(Application application);

}
