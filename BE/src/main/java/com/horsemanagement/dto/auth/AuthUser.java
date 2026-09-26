package com.horsemanagement.dto.auth;

import com.horsemanagement.security.Actor;
import java.util.List;

/**
 * Thông tin người dùng xác thực trả về cho Client khi Đăng nhập thành công và khi gọi GET /api/auth/me.
 *
 * @param userId      ID của người dùng
 * @param fullName    Họ và tên
 * @param email       Email tài khoản
 * @param role        Tên vai trò (Admin, Groom, Member, v.v.)
 * @param permissions Danh sách các quyền hạn cụ thể của người dùng
 */
public record AuthUser(int userId, String fullName, String email, String role, List<String> permissions) {

    /**
     * Chuyển đổi từ Actor trong SecurityContext sang AuthUser DTO.
     */
    public static AuthUser from(Actor actor) {
        return new AuthUser(
            actor.userId(),
            actor.fullName(),
            actor.email(),
            actor.role(),
            actor.authorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> !a.startsWith("ROLE_"))
                .sorted()
                .toList()
        );
    }
}
