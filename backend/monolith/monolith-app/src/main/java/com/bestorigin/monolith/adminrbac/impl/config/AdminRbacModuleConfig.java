package com.bestorigin.monolith.adminrbac.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminRbacModuleConfig {

    @Bean
    public Map<String, String> adminRbacOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-rbac",
                "packagePrefix", "com.bestorigin.monolith.adminrbac",
                "openApiJson", "/v3/api-docs/admin-rbac",
                "swaggerUi", "/swagger-ui/admin-rbac"
        );
    }
}
