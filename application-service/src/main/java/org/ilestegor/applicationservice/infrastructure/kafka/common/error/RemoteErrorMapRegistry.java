package org.ilestegor.applicationservice.infrastructure.kafka.common.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RemoteErrorMapRegistry {
    private  final Map<String, RemoteErrorMapper> errorMapper;

    public RemoteErrorMapRegistry(List<RemoteErrorMapper> mappers){
        this.errorMapper = mappers.stream().collect(Collectors.toUnmodifiableMap(RemoteErrorMapper::code, Function.identity()));
    }

    public RuntimeException map(ErrorPayload error){
        if (error == null)
            return new RemoteServiceException("UNKNOWN", "Remote error paylaod is null");

        RemoteErrorMapper mapper = errorMapper.get(error.code());

        if (mapper != null){
            log.error("RETURNING ERROR");
            return mapper.toException(error);
        }

        return new RemoteServiceException(
                error.code() == null ? "UNKNOWN" : error.code(),
                error.message() == null ? "Remote service exception" : error.message()
        );
    }
}
