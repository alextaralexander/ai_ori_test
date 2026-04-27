package com.bestorigin.monolith.adminplatform.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminPlatformModuleConfig {

    @Bean
    public Map<String, String> adminPlatformOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-platform",
                "packagePrefix", "com.bestorigin.monolith.adminplatform",
                "openApiJson", "/v3/api-docs/admin-platform",
                "swaggerUi", "/swagger-ui/admin-platform"
        );
    }
}