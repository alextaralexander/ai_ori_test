package com.bestorigin.monolith.publiccontent.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublicContentModuleConfig {

    @Bean
    public Map<String, String> publicContentOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "public-content",
                "openApiJson", "/v3/api-docs/public-content",
                "swaggerUi", "/swagger-ui/public-content"
        );
    }
}
