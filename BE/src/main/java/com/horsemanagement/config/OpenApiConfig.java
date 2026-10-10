package com.horsemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "Horse Management System API",
    version = "Phase 2F",
    description = "Local development API documentation. Veterinarian endpoints currently have no authentication or authorization."
))
public class OpenApiConfig {
}
