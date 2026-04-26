package com.bestorigin.monolith.catalog.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogModuleConfig {

    @Bean
    public Map<String, String> catalogOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "catalog",
                "openApiJson", "/v3/api-docs/catalog",
                "swaggerUi", "/swagger-ui/catalog"
        );
    }
}
