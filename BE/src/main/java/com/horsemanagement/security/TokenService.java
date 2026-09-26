package com.horsemanagement.security;

import com.horsemanagement.dao.auth.TokenDao;
import com.horsemanagement.dao.manager.RoleDao;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Service quản lý vòng đời của Access Token:
 * - Cấp phát (Issue) Bearer token ngẫu nhiên an toàn (32 bytes qua SecureRandom).
 * - Lưu mã băm SHA-256 của token vào cơ sở dữ liệu (Bảo vệ: DB bị lộ cũng không lộ token gốc).
 * - Xác thực token (Authenticate), kiểm tra hạn dùng và nạp danh sách quyền (Authorities).
 * - Thu hồi token khi đăng xuất (Revoke) hoặc đổi mật khẩu (RevokeUser).
 */
@Service
public class TokenService {

    private final TokenDao tokens;
    private final RoleDao roles;
    private final int hours;
    private final SecureRandom random = new SecureRandom();

    public TokenService(TokenDao tokens, RoleDao roles, @Value("${app.token-hours}") int hours) {
        this.tokens = tokens;
        this.roles = roles;
        this.hours = hours;
    }

    /**
     * DTO đại diện cho token vừa được cấp phát.
     */
    public record IssuedToken(String accessToken, String tokenType, Instant expiresAt) {}

    /**
     * Cấp mới một token cho người dùng sau khi đăng nhập thành công.
     * @param userId ID người dùng
     * @return IssuedToken chứa chuỗi accessToken và thời điểm hết hạn
     */
    public IssuedToken issue(int userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expires = Instant.now().plusSeconds(hours * 3600L);
        // Dọn dẹp token cũ hết hạn trong bảng ApiToken
        tokens.deleteExpired(Instant.now());
        // Lưu mã băm SHA-256 vào database
        tokens.insert(hash(raw), userId, expires);
        return new IssuedToken(raw, "Bearer", expires);
    }

    /**
     * Xác thực chuỗi token nhận từ HTTP Header.
     * @param token chuỗi token thô (43 ký tự Base64 URL-safe)
     * @return Optional chứa Actor nếu token hợp lệ và còn hạn, ngược lại Optional.empty()
     */
    public Optional<Actor> authenticate(String token) {
        if (token == null || token.length() != 43) {
            return Optional.empty();
        }
        return tokens.findActive(hash(token), Instant.now()).map(row -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            // Cấp role với prefix "ROLE_" theo quy ước của Spring Security
            authorities.add(new SimpleGrantedAuthority("ROLE_" + row.role()));
            // Nạp các quyền hạn cụ thể (Permissions) từ bảng ROLEPERMISSION
            for (String permission : roles.permissionsForUser(row.userId())) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
            return new Actor(row.userId(), row.fullName(), row.email(), row.role(), authorities);
        });
    }

    /**
     * Thu hồi 1 token cụ thể khi người dùng thực hiện Đăng xuất (Logout).
     */
    public void revoke(String raw) {
        tokens.deleteByHash(hash(raw));
    }

    /**
     * Thu hồi toàn bộ token của một người dùng (Dùng khi đổi mật khẩu hoặc khóa tài khoản).
     */
    public void revokeUser(int id) {
        tokens.deleteByUser(id);
    }

    /**
     * Băm chuỗi token bằng thuật toán SHA-256 để lưu trữ an toàn trong DB.
     */
    private String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

