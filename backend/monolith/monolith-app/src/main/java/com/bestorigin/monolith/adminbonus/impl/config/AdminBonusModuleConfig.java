package com.bestorigin.monolith.adminbonus.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBonusModuleConfig {

    @Bean
    public Map<String, String> adminBonusOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-bonus",
                "packagePrefix", "com.bestorigin.monolith.adminbonus",
                "openApiJson", "/v3/api-docs/admin-bonus",
                "swaggerUi", "/swagger-ui/admin-bonus"
        );
    }
}
