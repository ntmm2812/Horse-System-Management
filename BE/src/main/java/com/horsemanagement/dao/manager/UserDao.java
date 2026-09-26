package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.UserDto;
import com.horsemanagement.dto.manager.Requests.UserUpdate;
import java.util.*;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) quản lý tài khoản người dùng (dbo.[USER]):
 * - Xác thực đăng nhập, kiểm tra thông tin định danh và mật khẩu đã băm BCrypt.
 * - Tìm kiếm, lọc và phân trang người dùng theo tên, email, trạng thái và vai trò.
 * - Thêm mới tài khoản, cập nhật hồ sơ, thay đổi mật khẩu và đổi trạng thái (ACTIVE/INACTIVE/PENDING).
 * - Phê duyệt tài khoản đăng ký mới (approvePending).
 * - Đảm bảo an toàn hệ thống (không cho phép vô hiệu hóa Admin cuối cùng).
 */
@Repository
public class UserDao {

    private final Database db;

    public UserDao(Database db) {
        this.db = db;
    }

    private static final String USER_SELECT = """
        SELECT u.UserID AS userId, u.FullName AS fullName, u.Email AS email, u.RoleID AS roleId,
            r.RoleName AS role, u.AccountStatus AS status
        FROM dbo.[USER] u
        JOIN dbo.[ROLE] r ON r.RoleID = u.RoleID
        """;

    private static final String FILTER = " WHERE (u.FullName LIKE ? OR u.Email LIKE ?) AND (?='' OR u.AccountStatus=?) AND (?='' OR r.RoleName=?)";

    /**
     * DTO nội bộ chứa thông tin xác thực mật khẩu của người dùng (không trả về cho Controller/Client).
     */
    public record Credentials(int userId, String password, String status) {}

    /**
     * Tìm thông tin xác thực (ID, hash mật khẩu, trạng thái tài khoản) theo email.
     */
    public Optional<Credentials> findCredentials(String email) {
        return db.query("SELECT UserID AS userId, [Password] AS password, AccountStatus AS status FROM dbo.[USER] WHERE Email=?",
            Credentials.class, email).stream().findFirst();
    }

    /**
     * Lấy chuỗi mật khẩu đã băm của một tài khoản theo UserID.
     */
    public String password(int id) {
        return Database.text(db.one("SELECT [Password] AS password FROM dbo.[USER] WHERE UserID=?", id), "password");
    }

    /**
     * Tìm kiếm và phân trang danh sách người dùng với bộ lọc từ khóa, trạng thái và vai trò.
     */
    public List<UserDto> search(String search, String status, String role, int offset, int size) {
        String q = "%" + search + "%";
        return db.query(USER_SELECT + FILTER + " ORDER BY u.UserID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            UserDto.class, q, q, status, status, role, role, offset, size);
    }

    /**
     * Đếm tổng số người dùng khớp với bộ lọc tìm kiếm để tính phân trang.
     */
    public long count(String search, String status, String role) {
        String q = "%" + search + "%";
        return db.count("SELECT COUNT(*) FROM dbo.[USER] u JOIN dbo.[ROLE] r ON r.RoleID=u.RoleID" + FILTER,
            q, q, status, status, role, role);
    }

    /**
     * Lấy thông tin chi tiết một người dùng theo UserID.
     */
    public UserDto findById(int id) {
        return db.queryOne(USER_SELECT + " WHERE u.UserID=?", UserDto.class, id);
    }

    /**
     * Thêm tài khoản người dùng mới vào hệ thống và trả về UserID tự sinh.
     */
    public int insert(String fullName, String email, String passwordHash, int roleId, String status) {
        return db.insert("INSERT INTO dbo.[USER] (FullName, Email, [Password], RoleID, AccountStatus) VALUES (?,?,?,?,?)",
            fullName, email, passwordHash, roleId, status);
    }

    /**
     * Cập nhật thông tin cơ bản của người dùng (Họ tên, email, vai trò).
     */
    public void update(int id, UserUpdate input) {
        db.update("UPDATE dbo.[USER] SET FullName=?, Email=?, RoleID=? WHERE UserID=?",
            input.fullName().trim(), input.email().trim().toLowerCase(Locale.ROOT), input.roleId(), id);
    }

    /**
     * Cập nhật trạng thái tài khoản (ACTIVE, INACTIVE, SUSPENDED, v.v.).
     */
    public void setStatus(int id, String status) {
        db.update("UPDATE dbo.[USER] SET AccountStatus=? WHERE UserID=?", status, id);
    }

    /**
     * Phê duyệt hoặc từ chối tài khoản đang ở trạng thái chờ duyệt (PENDING).
     */
    public int approvePending(int id, String status) {
        return db.update("UPDATE dbo.[USER] SET AccountStatus=? WHERE UserID=? AND AccountStatus='PENDING'", status, id);
    }

    /**
     * Cập nhật mật khẩu mới (chuỗi băm BCrypt) cho người dùng.
     */
    public void setPassword(int id, String hash) {
        db.update("UPDATE dbo.[USER] SET [Password]=? WHERE UserID=?", hash, id);
    }

    /**
     * Đếm số lượng tài khoản Admin đang hoạt động và có mật khẩu BCrypt hợp lệ.
     * Ngăn chặn việc xóa hoặc vô hiệu hóa nhầm Admin cuối cùng trong hệ thống.
     */
    public long countUsableAdmins() {
        return db.count("SELECT COUNT(*) FROM dbo.[USER] u JOIN dbo.[ROLE] r ON r.RoleID=u.RoleID WHERE r.RoleName='Admin' AND u.AccountStatus='ACTIVE' AND u.[Password] LIKE '$2%'");
    }

    /**
     * Kiểm tra xem địa chỉ email đã tồn tại trong hệ thống hay chưa.
     */
    public boolean emailExists(String email) {
        return db.count("SELECT COUNT(*) FROM dbo.[USER] WHERE Email=?", email) > 0;
    }

    /**
     * Kiểm tra xem một người dùng có đang hoạt động (ACTIVE) với một vai trò cụ thể hay không.
     */
    public boolean isActiveWithRole(int id, String role) {
        return db.count("SELECT COUNT(*) FROM dbo.[USER] u JOIN dbo.[ROLE] r ON r.RoleID=u.RoleID WHERE u.UserID=? AND r.RoleName=? AND u.AccountStatus='ACTIVE'", id, role) == 1;
    }

    /**
     * Kiểm tra xem người dùng có phải là nhân viên nội bộ đang hoạt động hay không (Admin, Groom, Trainer, Vet).
     */
    public boolean isActiveStaff(int id) {
        return db.count("SELECT COUNT(*) FROM dbo.[USER] u JOIN dbo.[ROLE] r ON r.RoleID=u.RoleID WHERE u.UserID=? AND u.AccountStatus='ACTIVE' AND r.RoleName IN ('Admin','Groom','Trainer','Vet')", id) == 1;
    }
}
