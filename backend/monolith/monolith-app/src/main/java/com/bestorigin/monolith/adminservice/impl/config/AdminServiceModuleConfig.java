package com.bestorigin.monolith.adminservice.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminServiceModuleConfig {

    @Bean
    public Map<String, String> adminServiceOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-service",
                "packagePrefix", "com.bestorigin.monolith.adminservice",
                "openApiJson", "/v3/api-docs/admin-service",
                "swaggerUi", "/swagger-ui/admin-service"
        );
    }
}
