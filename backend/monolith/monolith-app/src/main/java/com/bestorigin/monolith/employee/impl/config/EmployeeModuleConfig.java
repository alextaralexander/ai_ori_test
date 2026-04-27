package com.bestorigin.monolith.employee.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmployeeModuleConfig {

    @Bean
    public Map<String, String> employeeModuleMetadata() {
        return Map.of(
                "moduleKey", "employee",
                "packagePrefix", "com.bestorigin.monolith.employee",
                "openApiJson", "/v3/api-docs/employee",
                "swaggerUi", "/swagger-ui/employee"
        );
    }
}
