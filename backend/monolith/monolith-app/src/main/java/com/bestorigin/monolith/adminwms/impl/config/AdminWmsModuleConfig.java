package com.bestorigin.monolith.adminwms.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminWmsModuleConfig {

    @Bean
    public Map<String, String> adminWmsOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-wms",
                "packagePrefix", "com.bestorigin.monolith.adminwms",
                "openApiJson", "/v3/api-docs/admin-wms",
                "swaggerUi", "/swagger-ui/admin-wms"
        );
    }
}
