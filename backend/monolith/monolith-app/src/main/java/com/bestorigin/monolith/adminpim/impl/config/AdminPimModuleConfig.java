package com.bestorigin.monolith.adminpim.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminPimModuleConfig {

    @Bean
    public Map<String, String> adminPimOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-pim",
                "packagePrefix", "com.bestorigin.monolith.adminpim",
                "openApiJson", "/v3/api-docs/admin-pim",
                "swaggerUi", "/swagger-ui/admin-pim"
        );
    }
}
