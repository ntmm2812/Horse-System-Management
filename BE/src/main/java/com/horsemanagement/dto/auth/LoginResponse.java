package com.horsemanagement.dto.auth;

import java.time.Instant;

/**
 * Phản hồi sau khi đăng nhập thành công:
 * @param accessToken Chuỗi Bearer token dùng để gửi kèm trong HTTP Authorization Header
 * @param tokenType   Loại token (luôn là "Bearer")
 * @param expiresAt   Thời điểm token hết hạn
 * @param user        Thông tin người dùng kèm vai trò và quyền hạn
 */
public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, AuthUser user) {}
