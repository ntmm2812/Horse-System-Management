package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.AuditLogDto;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) lưu trữ và truy vấn nhật ký kiểm toán (dbo.AuditLog):
 * - Lưu vết mọi hoạt động nhạy cảm trong hệ thống (thay đổi người dùng, vật tư, ngựa, tài chính, báo cáo).
 * - Tra cứu, lọc theo tài khoản người thực hiện hoặc theo bảng dữ liệu mục tiêu.
 */
@Repository
public class AuditDao {

    private final Database db;

    public AuditDao(Database db) {
        this.db = db;
    }

    private static final String FILTER = " WHERE (? IS NULL OR a.UserID=?) AND (?='' OR a.TargetTable=?)";

    /**
     * Thêm một bản ghi nhật ký kiểm toán mới với thời gian hiện tại của hệ thống.
     */
    public void insert(int userId, String action, String table, int targetId) {
        db.update("INSERT INTO dbo.AuditLog (UserID, [Action], TargetTable, TargetID, [TimeStamp]) VALUES (?,?,?,?,CURRENT_TIMESTAMP)",
            userId, action, table, targetId);
    }

    /**
     * Tìm kiếm và phân trang nhật ký kiểm toán theo người dùng hoặc bảng tác động.
     */
    public List<AuditLogDto> search(Integer userId, String table, int offset, int size) {
        return db.query("""
            SELECT a.LogID AS logId, a.UserID AS userId, u.FullName AS userName,
                a.[Action] AS action, a.TargetTable AS targetTable, a.TargetID AS targetId, a.[TimeStamp] AS timestamp
            FROM dbo.AuditLog a
            JOIN dbo.[USER] u ON u.UserID = a.UserID
            """ + FILTER + " ORDER BY a.LogID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            AuditLogDto.class, userId, userId, table, table, offset, size);
    }

    /**
     * Đếm tổng số bản ghi kiểm toán phù hợp để phân trang.
     */
    public long count(Integer userId, String table) {
        return db.count("SELECT COUNT(*) FROM dbo.AuditLog a" + FILTER, userId, userId, table, table);
    }
}
