package com.horsemanagement.dao.auth;

import com.horsemanagement.dao.support.Database;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) quản lý bảng dbo.ApiToken:
 * - Lưu trữ mã băm SHA-256 của token, thời gian hết hạn và liên kết với tài khoản người dùng.
 * - Truy vấn thông tin người dùng (PrincipalRow) từ token để phục vụ bộ lọc bảo mật.
 */
@Repository
public class TokenDao {

    private final Database db;

    public TokenDao(Database db) {
        this.db = db;
    }

    /**
     * Dữ liệu định danh tài khoản nạp từ token đang hoạt động.
     */
    public record PrincipalRow(int userId, String fullName, String email, String role) {}

    /**
     * Xóa các token đã hết hạn khỏi cơ sở dữ liệu để giải phóng bộ nhớ.
     */
    public void deleteExpired(Instant now) {
        db.update("DELETE FROM dbo.ApiToken WHERE ExpiresAt <= ?", java.sql.Timestamp.from(now));
    }

    /**
     * Thêm mới một token đã băm vào bảng ApiToken.
     */
    public void insert(String hash, int userId, Instant expires) {
        db.update("INSERT INTO dbo.ApiToken (TokenHash,UserID,ExpiresAt) VALUES (?,?,?)",
            hash, userId, java.sql.Timestamp.from(expires));
    }

    /**
     * Tìm kiếm thông tin người dùng đang hoạt động (ACTIVE) dựa trên mã băm token và còn hạn sử dụng.
     */
    public Optional<PrincipalRow> findActive(String hash, Instant now) {
        return db.query("""
            SELECT u.UserID AS userId, u.FullName AS fullName, u.Email AS email, r.RoleName AS role
            FROM dbo.ApiToken t
            JOIN dbo.[USER] u ON u.UserID = t.UserID
            JOIN dbo.[ROLE] r ON r.RoleID = u.RoleID
            WHERE t.TokenHash = ? AND t.ExpiresAt > ? AND u.AccountStatus = 'ACTIVE'
            """, PrincipalRow.class, hash, java.sql.Timestamp.from(now)).stream().findFirst();
    }

    /**
     * Xóa token cụ thể khi người dùng thực hiện Đăng xuất (Logout).
     */
    public void deleteByHash(String hash) {
        db.update("DELETE FROM dbo.ApiToken WHERE TokenHash = ?", hash);
    }

    /**
     * Xóa tất cả token của một người dùng (khi đổi mật khẩu hoặc thu hồi quyền truy cập).
     */
    public void deleteByUser(int id) {
        db.update("DELETE FROM dbo.ApiToken WHERE UserID = ?", id);
    }
}
