package com.horsemanagement.config;

import com.horsemanagement.exception.ApiProblem;
import com.horsemanagement.security.BearerTokenFilter;
import com.horsemanagement.security.TokenService;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

/**
 * Cấu hình bảo mật toàn hệ thống (Spring Security & RBAC).
 * - Sử dụng Stateless Authentication (Xác thực bằng Bearer Token lưu trong DB, không dùng Session/Cookie).
 * - Tắt CSRF vì API chỉ làm việc qua HTTP header Authorization: Bearer <token>.
 * - Cấu hình phân quyền nghiêm ngặt theo vai trò: Admin (Manager), Groom, Public.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Mã hóa mật khẩu người dùng sử dụng thuật toán BCrypt với độ mạnh 12 vòng lặp (cost factor 12).
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Chuỗi bộ lọc bảo mật chính (Security Filter Chain):
     * 1. Cấu hình CORS để Frontend (React/Vite) gọi API không bị chặn.
     * 2. Vô hiệu hóa CSRF và thiết lập Session Stateless.
     * 3. Quy định quyền truy cập từng đường dẫn API (Authorize Requests):
     *    - /api/auth/login, /api/auth/register: Cho phép tự do truy cập (Public).
     *    - /api/test: Endpoint kiểm tra kết nối API & DB (Public).
     *    - /api/manager/**: Chỉ tài khoản có vai trò 'Admin' (Quản lý) mới được truy cập.
     *    - /api/groom/**: Chỉ tài khoản có vai trò 'Groom' (Nhân viên chăm sóc) mới được truy cập.
     *    - /api/auth/me, logout, password: Bắt buộc đã đăng nhập (Authenticated).
     *    - Mọi request khác chưa cấu hình đều bị từ chối (denyAll).
     * 4. Bắt lỗi xác thực (401 Unauthorized) và phân quyền (403 Forbidden) trả về chuẩn RFC 7807 (Problem Details).
     * 5. Đặt bộ lọc BearerTokenFilter phía trước để giải mã token từ Header.
     */
    @Bean
    SecurityFilterChain security(HttpSecurity http, TokenService tokens, CorsConfigurationSource cors, ObjectMapper json) throws Exception {
        return http.cors(c -> c.configurationSource(cors))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/test").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/manager/**").hasRole("Admin")
                .requestMatchers("/api/groom/**").hasRole("Groom")
                .requestMatchers("/api/auth/me", "/api/auth/logout", "/api/auth/password").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((q, r, x) -> problem(json, q, r, 401, "Bạn cần đăng nhập hoặc token đã hết hạn."))
                .accessDeniedHandler((q, r, x) -> problem(json, q, r, 403, "Bạn không có quyền thực hiện thao tác này.")))
            .addFilterBefore(new BearerTokenFilter(tokens), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * Xuất phản hồi lỗi bảo mật (401/403) dưới dạng JSON chuẩn RFC 7807 (application/problem+json).
     */
    private static void problem(ObjectMapper json, HttpServletRequest request, HttpServletResponse response,
            int status, String detail) throws IOException {
        var problem = ApiProblem.of(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        if (status == 401) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
        json.writeValue(response.getOutputStream(), problem);
    }

    /**
     * Cấu hình chia sẻ tài nguyên nguồn gốc chéo (CORS):
     * Cho phép các domain FE (định nghĩa trong application.yml) gọi các phương thức RESTful.
     */
    @Bean
    CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        c.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", c);
        return source;
    }
}
