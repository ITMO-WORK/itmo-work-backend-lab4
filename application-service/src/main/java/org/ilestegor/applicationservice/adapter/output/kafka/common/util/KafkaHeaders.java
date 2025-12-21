package org.ilestegor.applicationservice.adapter.output.kafka.common.util;

import java.util.Map;

public class KafkaHeaders {
    public static final String AUTHORIZATION = "Authorization";

    private KafkaHeaders() {
    }

    public static Map<String, String> withJwt(String jwt) {
        return Map.of(AUTHORIZATION, jwt);
    }
}
