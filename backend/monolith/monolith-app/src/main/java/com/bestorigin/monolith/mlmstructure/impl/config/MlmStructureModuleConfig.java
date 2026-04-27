package com.bestorigin.monolith.mlmstructure.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MlmStructureModuleConfig {

    @Bean
    public Map<String, String> mlmStructureOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "mlm-structure",
                "packagePrefix", "com.bestorigin.monolith.mlmstructure",
                "openApiJson", "/v3/api-docs/mlm-structure",
                "swaggerUi", "/swagger-ui/mlm-structure"
        );
    }
}
