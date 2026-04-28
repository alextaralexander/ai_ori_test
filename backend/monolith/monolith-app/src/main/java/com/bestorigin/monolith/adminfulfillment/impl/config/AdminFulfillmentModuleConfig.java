package com.bestorigin.monolith.adminfulfillment.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminFulfillmentModuleConfig {

    @Bean
    public Map<String, String> adminFulfillmentOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-fulfillment",
                "packagePrefix", "com.bestorigin.monolith.adminfulfillment",
                "openApiJson", "/v3/api-docs/admin-fulfillment",
                "swaggerUi", "/swagger-ui/admin-fulfillment"
        );
    }
}
