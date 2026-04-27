package com.bestorigin.monolith.partneroffice.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PartnerOfficeModuleConfig {

    @Bean
    public Map<String, String> partnerOfficeModuleMetadata() {
        return Map.of(
                "moduleKey", "partner-office",
                "packagePrefix", "com.bestorigin.monolith.partneroffice",
                "openApiJson", "/v3/api-docs/partner-office",
                "swaggerUi", "/swagger-ui/partner-office"
        );
    }
}
