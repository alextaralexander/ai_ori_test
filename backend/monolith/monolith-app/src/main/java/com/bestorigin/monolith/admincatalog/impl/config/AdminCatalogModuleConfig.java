package com.bestorigin.monolith.admincatalog.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminCatalogModuleConfig {

    @Bean
    public Map<String, String> adminCatalogOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-catalog",
                "packagePrefix", "com.bestorigin.monolith.admincatalog",
                "openApiJson", "/v3/api-docs/admin-catalog",
                "swaggerUi", "/swagger-ui/admin-catalog"
        );
    }
}
