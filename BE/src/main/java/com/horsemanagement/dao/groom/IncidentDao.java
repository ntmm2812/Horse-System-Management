package com.horsemanagement.dao.groom;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.groom.IncidentInput;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) quản lý báo cáo sự cố (dbo.INCIDENTREPORT):
 * - Cho phép Groom tạo báo cáo khi ngựa gặp chấn thương, sự cố hoặc hành vi bất thường.
 * - Cho phép Groom xem danh sách và chi tiết các sự cố do chính mình báo cáo.
 * - Cho phép Quản lý (Manager) xem toàn bộ sự cố trong chuồng ngựa.
 * - Đính kèm đường dẫn khóa ảnh (ImagePath) vào báo cáo sự cố.
 */
@Repository
public class IncidentDao {

    private final Database db;

    public IncidentDao(Database db) {
        this.db = db;
    }

    private static final String INCIDENT_SELECT = """
        SELECT i.ReportID AS reportId, i.HorseID AS horseId, h.[Name] AS horseName,
            i.GroomID AS groomId, i.IncidentType AS incidentType, i.[Description] AS description,
            i.[Date] AS date, i.ImagePath AS imagePath
        FROM dbo.INCIDENTREPORT i
        JOIN dbo.Horse h ON h.HorseID = i.HorseID
        """;

    /**
     * Bản ghi chứa đầy đủ trường thông tin của một báo cáo sự cố.
     */
    public record IncidentRow(int reportId, int horseId, String horseName, int groomId,
                             String incidentType, String description, LocalDate date, String imagePath) {}

    /**
     * Tạo báo cáo sự cố mới và trả về ReportID vừa sinh.
     */
    public int insert(int groomId, IncidentInput input) {
        return db.insert("""
            INSERT INTO dbo.INCIDENTREPORT (HorseID, GroomID, IncidentType, ImagePath, [Date], [Description])
            VALUES (?,?,?,NULL,?,?)
            """, input.horseId(), groomId, input.incidentType(), input.date(), input.description());
    }

    /**
     * Tìm báo cáo sự cố thuộc sở hữu của Groom chỉ định theo ReportID.
     */
    public IncidentRow findOwned(int groomId, int id) {
        return db.queryOne(INCIDENT_SELECT + " WHERE i.ReportID = ? AND i.GroomID = ?", IncidentRow.class, id, groomId);
    }

    /**
     * Tìm báo cáo sự cố theo ReportID (Dành cho Quản lý / Manager xem mọi báo cáo).
     */
    public IncidentRow findById(int id) {
        return db.queryOne(INCIDENT_SELECT + " WHERE i.ReportID = ?", IncidentRow.class, id);
    }

    /**
     * Danh sách phân trang các sự cố do một Groom cụ thể đã tạo.
     */
    public List<IncidentRow> findOwned(int groomId, int offset, int size) {
        return db.query(INCIDENT_SELECT + " WHERE i.GroomID = ? ORDER BY i.ReportID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            IncidentRow.class, groomId, offset, size);
    }

    /**
     * Đếm tổng số sự cố do Groom cụ thể đã tạo.
     */
    public long countOwned(int groomId) {
        return db.count("SELECT COUNT(*) FROM dbo.INCIDENTREPORT WHERE GroomID = ?", groomId);
    }

    /**
     * Danh sách phân trang tất cả các sự cố trong hệ thống (Dành cho Quản lý).
     */
    public List<IncidentRow> findAll(int offset, int size) {
        return db.query(INCIDENT_SELECT + " ORDER BY i.ReportID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            IncidentRow.class, offset, size);
    }

    /**
     * Đếm tổng số sự cố trong toàn hệ thống.
     */
    public long countAll() {
        return db.count("SELECT COUNT(*) FROM dbo.INCIDENTREPORT");
    }

    /**
     * Cập nhật khóa file ảnh vào báo cáo sự cố (chỉ thực hiện nếu chưa có ảnh).
     */
    public int attachImage(int groomId, int id, String key) {
        return db.update("UPDATE dbo.INCIDENTREPORT SET ImagePath = ? WHERE ReportID = ? AND GroomID = ? AND ImagePath IS NULL",
            key, id, groomId);
    }
}
