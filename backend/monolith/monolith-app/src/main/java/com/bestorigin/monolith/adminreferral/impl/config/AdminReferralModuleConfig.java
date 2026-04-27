package com.bestorigin.monolith.adminreferral.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminReferralModuleConfig {

    @Bean
    public Map<String, String> adminReferralOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-referral",
                "packagePrefix", "com.bestorigin.monolith.adminreferral",
                "openApiJson", "/v3/api-docs/admin-referral",
                "swaggerUi", "/swagger-ui/admin-referral"
        );
    }
}
