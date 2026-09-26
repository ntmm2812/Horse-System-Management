package com.horsemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger / OpenAPI 3:
 * - Cung cấp giao diện web điều hướng API và khung nhập liệu tương tác trực tiếp tại: /swagger-ui/index.html
 * - Cho phép nhập Bearer token để gọi và kiểm thử các API phân quyền của Club Manager và Groom.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Horse Management System - API & Khung nhập liệu (SWP391)")
                .description("Hệ thống quản lý chuồng ngựa — Điều hướng và khung form nhập liệu tương tác trực quan cho Club Manager và Groom")
                .version("1.0.0")
                .contact(new Contact().name("SWP391 Team").email("support@horsemanagement.com")))
            .addSecurityItem(new SecurityRequirement().addList("BearerToken"))
            .schemaRequirement("BearerToken", new SecurityScheme()
                .name("Authorization")
                .description("Nhập Bearer Token lấy từ API /api/auth/login. Ví dụ: Bearer <token>")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("Token"));
    }
}
