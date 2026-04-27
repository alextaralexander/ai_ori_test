package com.bestorigin.monolith.partnerreporting.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PartnerReportingModuleConfig {

    @Bean
    public Map<String, String> partnerReportingOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "partner-reporting",
                "packagePrefix", "com.bestorigin.monolith.partnerreporting",
                "openApiJson", "/v3/api-docs/partner-reporting",
                "swaggerUi", "/swagger-ui/partner-reporting"
        );
    }
}
