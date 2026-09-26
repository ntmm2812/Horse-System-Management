package com.horsemanagement.service;

import com.horsemanagement.dao.manager.UserDao;
import com.horsemanagement.dao.manager.RoleDao;
import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.auth.AuthRequests.*;
import com.horsemanagement.exception.ApiException;
import com.horsemanagement.security.Actor;
import com.horsemanagement.security.TokenService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/**
 * Service xử lý nghiệp vụ xác thực (Authentication) & Quản lý tài khoản bảo mật:
 * - Đăng nhập, kiểm tra mật khẩu BCrypt và trạng thái tài khoản
 * - Cấp phát Bearer token ngẫu nhiên (lưu hash SHA-256)
 * - Đăng ký tài khoản Owner chờ duyệt
 * - Đổi mật khẩu và thu hồi phiên đăng nhập
 * - Khởi tạo tài khoản Admin đầu tiên (Bootstrap Admin)
 */
@Service
public class AuthService {
    private final UserDao users;
    private final RoleDao roles;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final AuditService audit;

    public AuthService(UserDao users, RoleDao roles, PasswordEncoder passwords, TokenService tokens, AuditService audit) {
        this.users = users;
        this.roles = roles;
        this.passwords = passwords;
        this.tokens = tokens;
        this.audit = audit;
    }

    /**
     * Nghiệp vụ đăng nhập:
     * 1. Tìm thông tin đăng nhập theo email chuẩn hóa
     * 2. Kiểm tra mật khẩu băm BCrypt ($2...)
     * 3. Kiểm tra trạng thái tài khoản (chỉ cho phép trạng thái ACTIVE)
     * 4. Ghi nhật ký đăng nhập (AuditLog)
     * 5. Sinh Bearer token ngẫu nhiên, nạp quyền (Role & Permissions) và trả về LoginResponse
     */
    @Transactional
    public LoginResponse login(Login input) {
        var user = users.findCredentials(normalizeEmail(input.email()))
                .orElseThrow(() -> new ApiException(401, "Email hoặc mật khẩu không đúng."));
        
        String hash = user.password();
        if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72 
                || !hash.startsWith("$2") 
                || !passwords.matches(input.password(), hash)) {
            throw new ApiException(401, "Email hoặc mật khẩu không đúng.");
        }

        if (!"ACTIVE".equals(user.status())) {
            throw new ApiException(403, "Tài khoản chưa được duyệt hoặc đã bị khóa.");
        }

        audit.record(user.userId(), "LOGIN", "USER", user.userId());
        var token = tokens.issue(user.userId());
        var actor = tokens.authenticate(token.accessToken()).orElseThrow();
        return new LoginResponse(token.accessToken(), token.tokenType(), token.expiresAt(), AuthUser.from(actor));
    }

    /**
     * Nghiệp vụ đăng ký tài khoản cho chủ ngựa (Owner):
     * Mặc định role là Owner, trạng thái ban đầu là PENDING (chờ Club Manager duyệt).
     */
    @Transactional
    public RegistrationDto register(Register input) {
        int roleId = roles.findByName("Owner").roleId();
        int id = users.insert(input.fullName().trim(), normalizeEmail(input.email()), encode(input.password()), roleId, "PENDING");
        audit.record(id, "REGISTER", "USER", id);
        return new RegistrationDto(id, "PENDING", "Đăng ký thành công, chờ Admin duyệt.");
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập từ Actor trong SecurityContext.
     */
    public AuthUser me(Actor actor) { 
        return AuthUser.from(actor); 
    }

    /**
     * Thu hồi token khi người dùng đăng xuất.
     */
    @Transactional
    public void logout(String rawToken) { 
        tokens.revoke(rawToken); 
    }

    /**
     * Đổi mật khẩu: So khớp mật khẩu hiện tại, sau đó cập nhật mật khẩu mới và thu hồi toàn bộ token.
     */
    @Transactional
    public void password(Actor actor, PasswordChange input) {
        if (!passwords.matches(input.currentPassword(), users.password(actor.userId()))) {
            throw new ApiException(400, "Mật khẩu hiện tại không đúng.");
        }
        users.setPassword(actor.userId(), encode(input.newPassword()));
        tokens.revokeUser(actor.userId());
        audit.record(actor.userId(), "CHANGE_PASSWORD", "USER", actor.userId());
    }

    /**
     * Khởi tạo tài khoản Club Manager (Role Admin) đầu tiên nếu hệ thống chưa có Admin nào hoạt động.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void bootstrapAdmin(String email, String password) {
        if (!email.matches("[^\\s@]+@[^\\s@]+[.][^\\s@]+") || email.length() > 254 || password.length() < 12
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Cần BOOTSTRAP_EMAIL hợp lệ và BOOTSTRAP_PASSWORD từ 12 ký tự, tối đa 72 byte.");
        }
        if (users.countUsableAdmins() > 0) return;

        String normalized = normalizeEmail(email);
        if (users.emailExists(normalized)) {
            throw new IllegalStateException("Email bootstrap đã tồn tại. Dùng email mới để tránh ghi đè tài khoản.");
        }

        int roleId = roles.findByName("Admin").roleId();
        int id = users.insert("Club Manager", normalized, encode(password), roleId, "ACTIVE");
        audit.record(id, "BOOTSTRAP_ADMIN", "USER", id);
    }

    /**
     * Mã hóa mật khẩu bằng BCrypt (giới hạn tối đa 72 byte UTF-8 theo chuẩn thuật toán BCrypt).
     */
    private String encode(String password) {
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(400, "Mật khẩu không được vượt quá 72 byte UTF-8.");
        }
        return passwords.encode(password);
    }

    /**
     * Chuẩn hóa email: xóa khoảng trắng thừa và chuyển về chữ thường.
     */
    private static String normalizeEmail(String email) { 
        return email.trim().toLowerCase(Locale.ROOT); 
    }
}
