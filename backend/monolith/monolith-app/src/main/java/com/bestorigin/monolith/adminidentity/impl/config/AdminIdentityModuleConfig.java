package com.bestorigin.monolith.adminidentity.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminIdentityModuleConfig {

    @Bean
    public Map<String, String> adminIdentityOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-identity",
                "packagePrefix", "com.bestorigin.monolith.adminidentity",
                "openApiJson", "/v3/api-docs/admin-identity",
                "swaggerUi", "/swagger-ui/admin-identity"
        );
    }
}
