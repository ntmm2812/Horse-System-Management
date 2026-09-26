package com.horsemanagement.dto.auth;

import jakarta.validation.constraints.*;

/**
 * Các DTO dữ liệu đầu vào cho nhóm chức năng Xác thực (Authentication):
 * - Đăng nhập (Login)
 * - Đăng ký tài khoản (Register)
 * - Đổi mật khẩu (PasswordChange)
 */
public final class AuthRequests {
    private AuthRequests() {}

    /**
     * Yêu cầu đăng nhập hệ thống bằng email và mật khẩu.
     */
    public record Login(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 254)
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 72)
        String password
    ) {}

    /**
     * Yêu cầu đăng ký tài khoản mới của khách hàng/thành viên.
     */
    public record Register(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 254)
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 10, max = 72, message = "Mật khẩu phải từ 10 đến 72 ký tự")
        String password
    ) {}

    /**
     * Yêu cầu thay đổi mật khẩu khi đã đăng nhập.
     */
    public record PasswordChange(
        @NotBlank(message = "Mật khẩu hiện tại không được để trống")
        @Size(max = 72)
        String currentPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 10, max = 72, message = "Mật khẩu mới phải từ 10 đến 72 ký tự")
        String newPassword
    ) {}
}
