package com.horsemanagement.dto.auth;

/**
 * Phản hồi sau khi khách hàng đăng ký tài khoản thành công:
 * @param userId  ID của tài khoản vừa tạo
 * @param status  Trạng thái tài khoản (ví dụ: 'PENDING' - chờ Admin duyệt)
 * @param message Thông báo hướng dẫn người dùng
 */
public record RegistrationDto(int userId, String status, String message) {}
