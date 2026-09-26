package com.horsemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Lớp khởi động chính (Main Application) của hệ thống Quản lý Chuồng Ngựa (Horse Management System).
 * - Sử dụng Spring Boot 3.x
 * - Loại trừ UserDetailsServiceAutoConfiguration vì hệ thống tự xác thực qua TokenDao và BearerTokenFilter
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class HorseManagementApplication {

    /**
     * Điểm bắt đầu thực thi ứng dụng backend.
     * @param args tham số dòng lệnh khi khởi động
     */
    public static void main(String[] args) {
        SpringApplication.run(HorseManagementApplication.class, args);
    }
}

