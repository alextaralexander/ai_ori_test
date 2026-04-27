package com.bestorigin.monolith.profile.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileModuleConfig {

    @Bean
    public Map<String, String> profileOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "profile",
                "packagePrefix", "com.bestorigin.monolith.profile",
                "openApiJson", "/v3/api-docs/profile",
                "swaggerUi", "/swagger-ui/profile"
        );
    }
}
