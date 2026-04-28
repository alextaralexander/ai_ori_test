package com.bestorigin.monolith.adminbenefitprogram.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBenefitProgramModuleConfig {

    @Bean
    public Map<String, String> adminBenefitProgramOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-benefit-program",
                "packagePrefix", "com.bestorigin.monolith.adminbenefitprogram",
                "openApiJson", "/v3/api-docs/admin-benefit-program",
                "swaggerUi", "/swagger-ui/admin-benefit-program"
        );
    }
}
