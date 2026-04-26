package com.bestorigin.monolith.partneronboarding.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PartnerOnboardingModuleConfig {

    @Bean
    public Map<String, String> partnerOnboardingOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "partner-onboarding",
                "openApiJson", "/v3/api-docs/partner-onboarding",
                "swaggerUi", "/swagger-ui/partner-onboarding"
        );
    }
}
