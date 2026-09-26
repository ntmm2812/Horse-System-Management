package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.HorseDto;
import com.horsemanagement.dto.manager.Requests.HorseInput;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) quản lý hồ sơ và dữ liệu ngựa (dbo.Horse):
 * - Tìm kiếm, phân trang danh sách ngựa còn hoạt động (Archived = 0).
 * - Xem chi tiết, thêm mới, cập nhật hồ sơ ngựa và dòng dõi.
 * - Khởi tạo trạng thái sức khỏe ban đầu trong bảng dbo.HEALTHSTATUS.
 * - Lưu trữ/xóa mềm (Soft delete) hồ sơ ngựa bằng cờ Archived = 1.
 */
@Repository
public class HorseDao {

    private final Database db;

    public HorseDao(Database db) {
        this.db = db;
    }

    private static final String HORSE_SELECT = """
        SELECT h.HorseID AS horseId, h.[Name] AS name, h.Gender AS gender, h.Age AS age, h.[Weight] AS weight,
            h.Lineage AS lineage, h.OwnerID AS ownerId, u.FullName AS ownerName, h.Archived AS archived,
            hs.[Status] AS healthStatus
        FROM dbo.Horse h
        JOIN dbo.[USER] u ON u.UserID = h.OwnerID
        LEFT JOIN dbo.HEALTHSTATUS hs ON hs.HorseID = h.HorseID
        """;

    /**
     * Tìm kiếm và phân trang danh sách ngựa chưa bị xóa mềm (Archived = 0).
     */
    public List<HorseDto> findActive(String search, int offset, int size) {
        return db.query(HORSE_SELECT + " WHERE h.Archived = 0 AND h.[Name] LIKE ? ORDER BY h.HorseID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            HorseDto.class, "%" + search + "%", offset, size);
    }

    /**
     * Đếm tổng số ngựa thỏa mãn điều kiện tìm kiếm để phân trang.
     */
    public long countActive(String search) {
        return db.count("SELECT COUNT(*) FROM dbo.Horse WHERE Archived = 0 AND [Name] LIKE ?", "%" + search + "%");
    }

    /**
     * Lấy thông tin chi tiết một chú ngựa theo ID (nếu chưa bị xóa mềm).
     */
    public HorseDto findActiveById(int id) {
        return db.queryOne(HORSE_SELECT + " WHERE h.HorseID = ? AND h.Archived = 0", HorseDto.class, id);
    }

    /**
     * Thêm mới một chú ngựa vào hệ thống và trả về ID tự tăng vừa tạo.
     */
    public int insert(HorseInput input) {
        return db.insert("INSERT INTO dbo.Horse ([Name], Gender, Age, [Weight], Lineage, OwnerID) VALUES (?,?,?,?,?,?)",
            input.name().trim(), input.gender(), input.age(), input.weight(), input.lineage(), input.ownerId());
    }

    /**
     * Khởi tạo bản ghi trạng thái sức khỏe mặc định cho ngựa mới tạo.
     */
    public void initializeHealth(int id) {
        db.update("INSERT INTO dbo.HEALTHSTATUS (HorseID, [Status], LastUpdate) VALUES (?, 'Unknown', CAST(CURRENT_TIMESTAMP AS DATE))", id);
    }

    /**
     * Cập nhật thông tin chi tiết của chú ngựa.
     */
    public int update(int id, HorseInput input) {
        return db.update("UPDATE dbo.Horse SET [Name]=?, Gender=?, Age=?, [Weight]=?, Lineage=?, OwnerID=? WHERE HorseID=? AND Archived=0",
            input.name().trim(), input.gender(), input.age(), input.weight(), input.lineage(), input.ownerId(), id);
    }

    /**
     * Xóa mềm chú ngựa (đặt cờ Archived = 1 để không hiển thị nhưng vẫn giữ lại lịch sử chăm sóc và chi phí).
     */
    public int archive(int id) {
        return db.update("UPDATE dbo.Horse SET Archived=1 WHERE HorseID=? AND Archived=0", id);
    }
}
