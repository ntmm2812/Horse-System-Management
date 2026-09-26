package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.SupplyDto;
import com.horsemanagement.dto.manager.Requests.SupplyInput;
import com.horsemanagement.dto.manager.Requests.SupplyUpdate;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) quản lý kho vật tư và thức ăn (dbo.Supply):
 * - Tìm kiếm, lọc và phân trang các vật tư còn sử dụng (Archived = 0).
 * - Thêm mới vật tư, cập nhật thông tin và người quản lý.
 * - Điều chỉnh số lượng tồn kho (nhập/xuất) với cơ chế khóa lạc quan (Optimistic Locking qua trường Version).
 * - Ngăn chặn tồn kho âm (Cast to BigInt >= 0) và hiện tượng ghi đè đồng thời (Lost Update).
 * - Xóa mềm vật tư (Archived = 1).
 */
@Repository
public class SupplyDao {

    private final Database db;

    public SupplyDao(Database db) {
        this.db = db;
    }

    private static final String SUPPLY_SELECT = """
        SELECT s.SupplyID AS supplyId, s.ItemName AS itemName, s.[Type] AS type, s.QuantityInStock AS quantityInStock,
            s.ManagedBy AS managedBy, u.FullName AS managerName, s.[Version] AS version
        FROM dbo.Supply s
        JOIN dbo.[USER] u ON u.UserID = s.ManagedBy
        """;

    /**
     * Tìm kiếm và phân trang danh sách vật tư chưa bị xóa mềm.
     */
    public List<SupplyDto> search(String search, int offset, int size) {
        return db.query(SUPPLY_SELECT + " WHERE s.Archived=0 AND s.ItemName LIKE ? ORDER BY s.SupplyID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            SupplyDto.class, "%" + search + "%", offset, size);
    }

    /**
     * Đếm tổng số vật tư phù hợp với từ khóa tìm kiếm để phân trang.
     */
    public long count(String search) {
        return db.count("SELECT COUNT(*) FROM dbo.Supply WHERE Archived=0 AND ItemName LIKE ?", "%" + search + "%");
    }

    /**
     * Lấy thông tin chi tiết một vật tư theo SupplyID (chưa bị xóa mềm).
     */
    public SupplyDto findActiveById(int id) {
        return db.queryOne(SUPPLY_SELECT + " WHERE s.SupplyID=? AND s.Archived=0", SupplyDto.class, id);
    }

    /**
     * Thêm mới một vật tư vào kho và trả về ID tự sinh.
     */
    public int insert(SupplyInput input) {
        return db.insert("INSERT INTO dbo.Supply (ItemName, [Type], QuantityInStock, ManagedBy) VALUES (?,?,?,?)",
            input.itemName().trim(), input.type(), input.quantityInStock(), input.managedBy());
    }

    /**
     * Cập nhật thông tin vật tư với cơ chế kiểm tra Version để tránh xung đột sửa đổi đồng thời.
     */
    public int update(int id, SupplyUpdate input) {
        return db.update("UPDATE dbo.Supply SET ItemName=?, [Type]=?, ManagedBy=?, [Version]=[Version]+1 WHERE SupplyID=? AND [Version]=? AND Archived=0",
            input.itemName().trim(), input.type(), input.managedBy(), id, input.version());
    }

    /**
     * Điều chỉnh số lượng tồn kho (delta > 0: nhập kho, delta < 0: xuất kho).
     * Kiểm tra Version khóa lạc quan và đảm bảo số lượng tồn kho mới không bao giờ âm.
     */
    public int adjustStock(int id, int delta, int version) {
        return db.update("UPDATE dbo.Supply SET QuantityInStock=QuantityInStock+?, [Version]=[Version]+1 WHERE SupplyID=? AND Archived=0 AND [Version]=? AND CAST(QuantityInStock AS BIGINT)+? BETWEEN 0 AND 2147483647",
            delta, id, version, delta);
    }

    /**
     * Xóa mềm vật tư (Archived = 1) đồng thời tăng Version.
     */
    public int archive(int id, int version) {
        return db.update("UPDATE dbo.Supply SET Archived=1, [Version]=[Version]+1 WHERE SupplyID=? AND [Version]=? AND Archived=0",
            id, version);
    }
}
