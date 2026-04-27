package com.bestorigin.monolith.platformexperience.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlatformExperienceModuleConfig {

    @Bean
    public Map<String, String> platformExperienceOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "platform-experience",
                "packagePrefix", "com.bestorigin.monolith.platformexperience",
                "openApiJson", "/v3/api-docs/platform-experience",
                "swaggerUi", "/swagger-ui/platform-experience"
        );
    }
}
