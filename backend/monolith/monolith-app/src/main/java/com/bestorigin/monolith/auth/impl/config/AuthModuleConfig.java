package com.bestorigin.monolith.auth.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthModuleConfig {

    @Bean
    public Map<String, String> authModuleMetadata() {
        return Map.of(
                "moduleKey", "auth",
                "packagePrefix", "com.bestorigin.monolith.auth",
                "openApiJson", "/v3/api-docs/auth",
                "swaggerUi", "/swagger-ui/auth"
        );
    }
}
