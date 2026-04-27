package com.bestorigin.monolith.admincms.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminCmsModuleConfig {

    @Bean
    public Map<String, String> adminCmsOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-cms",
                "packagePrefix", "com.bestorigin.monolith.admincms",
                "openApiJson", "/v3/api-docs/admin-cms",
                "swaggerUi", "/swagger-ui/admin-cms"
        );
    }
}
