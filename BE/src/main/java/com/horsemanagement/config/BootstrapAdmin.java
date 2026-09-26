package com.horsemanagement.config;

import com.horsemanagement.service.AuthService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tự động khởi tạo tài khoản Quản trị viên (Admin) mặc định khi ứng dụng khởi chạy lần đầu.
 * - Chỉ chạy nếu cấu hình 'app.bootstrap.enabled=true'.
 * - Khởi tạo tài khoản với email và mật khẩu từ application.yml / biến môi trường.
 */
@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class BootstrapAdmin implements ApplicationRunner {

    private final AuthService auth;
    private final String email;
    private final String password;

    public BootstrapAdmin(AuthService auth,
                          @Value("${app.bootstrap.email}") String email,
                          @Value("${app.bootstrap.password}") String password) {
        this.auth = auth;
        this.email = email;
        this.password = password;
    }

    /**
     * Chạy ngay sau khi Spring Boot hoàn tất khởi động để tạo Admin nếu chưa tồn tại.
     */
    @Override
    public void run(ApplicationArguments args) {
        auth.bootstrapAdmin(email, password);
    }
}

