package com.bestorigin.monolith.delivery.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryModuleConfig {

    @Bean
    public Map<String, String> deliveryOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "delivery",
                "packagePrefix", "com.bestorigin.monolith.delivery",
                "openApiJson", "/v3/api-docs/delivery",
                "swaggerUi", "/swagger-ui/delivery"
        );
    }
}
