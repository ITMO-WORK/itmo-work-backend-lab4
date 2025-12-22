package org.ilestegor.applicationservice.mapper;

import org.ilestegor.applicationservice.adapter.input.web.dto.ApplicationDto;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.domain.Application;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {


    @Mapping(target = "vacancyId", source = "vacancyId")
    @Mapping(target = "userId", source = "userId")
    ApplicationDto fromApplicationtoApplicationDto(Application application);

}
