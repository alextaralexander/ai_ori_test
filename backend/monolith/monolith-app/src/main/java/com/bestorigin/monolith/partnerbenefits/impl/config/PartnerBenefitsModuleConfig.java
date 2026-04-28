package com.bestorigin.monolith.partnerbenefits.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PartnerBenefitsModuleConfig {

    @Bean
    public Map<String, String> partnerBenefitsOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "partner-benefits",
                "packagePrefix", "com.bestorigin.monolith.partnerbenefits",
                "openApiJson", "/v3/api-docs/partner-benefits",
                "swaggerUi", "/swagger-ui/partner-benefits"
        );
    }
}
