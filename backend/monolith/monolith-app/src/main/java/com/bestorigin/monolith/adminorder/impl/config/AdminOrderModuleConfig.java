package com.bestorigin.monolith.adminorder.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminOrderModuleConfig {

    @Bean
    public Map<String, String> adminOrderOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-order",
                "packagePrefix", "com.bestorigin.monolith.adminorder",
                "openApiJson", "/v3/api-docs/admin-order",
                "swaggerUi", "/swagger-ui/admin-order"
        );
    }
}
