package com.bestorigin.monolith.bonuswallet.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BonusWalletModuleConfig {

    @Bean
    public Map<String, String> bonusWalletOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "bonus-wallet",
                "packagePrefix", "com.bestorigin.monolith.bonuswallet",
                "openApiJson", "/v3/api-docs/bonus-wallet",
                "swaggerUi", "/swagger-ui/bonus-wallet"
        );
    }
}
