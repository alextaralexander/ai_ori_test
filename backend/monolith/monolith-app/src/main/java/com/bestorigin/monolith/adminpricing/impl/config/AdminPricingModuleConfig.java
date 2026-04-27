package com.bestorigin.monolith.adminpricing.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminPricingModuleConfig {

    @Bean
    public Map<String, String> adminPricingOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-pricing",
                "packagePrefix", "com.bestorigin.monolith.adminpricing",
                "openApiJson", "/v3/api-docs/admin-pricing",
                "swaggerUi", "/swagger-ui/admin-pricing"
        );
    }
}
