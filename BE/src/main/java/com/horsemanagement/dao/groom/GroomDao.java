package com.horsemanagement.dao.groom;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.groom.*;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) phục vụ các tác vụ của Nhân viên chăm sóc ngựa (Groom):
 * - Quản lý danh sách các chuồng, ngựa được phân công phụ trách trong ngày (dbo.STABLEASSIGNMENT, dbo.CARETASK).
 * - Tra cứu chế độ dinh dưỡng, khẩu phần ăn hàng ngày của ngựa (dbo.FEEDINGPLAN).
 * - Xem danh sách công việc chăm sóc định kỳ, đánh dấu hoàn thành nhiệm vụ (dbo.CARETASK).
 * - Xem lịch trình tập luyện / làm việc của ngựa trong ngày (dbo.TRAININGSCHEDULE).
 */
@Repository
public class GroomDao {

    private final Database db;

    public GroomDao(Database db) {
        this.db = db;
    }

    private static final String ASSIGNED = """
        (EXISTS (SELECT 1 FROM dbo.CARETASK c WHERE c.HorseID = h.HorseID AND c.GroomID = ? AND c.[Date] = ?)
         OR EXISTS (SELECT 1 FROM dbo.TRAININGSCHEDULE s WHERE s.HorseID = h.HorseID AND s.GroomID = ? AND s.[Date] = ?))
        """;

    private static final String TASK_SELECT = "SELECT c.TaskID AS taskId, c.HorseID AS horseId, h.[Name] AS horseName, c.TaskType AS taskType, c.[Status] AS completed, c.[Date] AS date FROM dbo.CARETASK c JOIN dbo.Horse h ON h.HorseID = c.HorseID";
    private static final String TASK_FILTER = " WHERE c.GroomID = ? AND c.[Date] = ? AND (? IS NULL OR c.[Status] = ?) AND h.Archived = 0";

    /**
     * Danh sách các chuồng ngựa và thông tin ô chuồng mà Groom được phân công phụ trách theo ngày.
     */
    public List<StableDto> stables(int groomId, LocalDate date, int offset, int size) {
        return db.query("""
            SELECT h.HorseID AS horseId, h.[Name] AS horseName, sa.StableID AS stableId,
                sa.StallNumber AS stallNumber, sa.DailyRoutine AS dailyRoutine
            FROM dbo.Horse h
            LEFT JOIN dbo.STABLEASSIGNMENT sa ON sa.HorseID = h.HorseID
                AND sa.StableID = (SELECT MAX(sa2.StableID) FROM dbo.STABLEASSIGNMENT sa2 WHERE sa2.HorseID = h.HorseID)
            WHERE h.Archived = 0 AND
            """ + ASSIGNED + " ORDER BY h.HorseID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            StableDto.class, groomId, date, groomId, date, offset, size);
    }

    /**
     * Đếm tổng số ngựa được phân công cho Groom trong ngày.
     */
    public long countStables(int groomId, LocalDate date) {
        return db.count("SELECT COUNT(*) FROM dbo.Horse h WHERE h.Archived = 0 AND " + ASSIGNED, groomId, date, groomId, date);
    }

    /**
     * Kiểm tra xem một chú ngựa cụ thể có được giao cho Groom này phụ trách vào ngày chỉ định hay không.
     */
    public boolean isAssigned(int groomId, int horseId, LocalDate date) {
        return db.count("SELECT COUNT(*) FROM dbo.Horse h WHERE h.HorseID = ? AND h.Archived = 0 AND " + ASSIGNED,
            horseId, groomId, date, groomId, date) == 1;
    }

    /**
     * Lấy kế hoạch dinh dưỡng (thức ăn tinh, cỏ, vitamin, bữa ăn) của một chú ngựa.
     */
    public List<FeedingPlanDto> feeding(int horseId) {
        return db.query("SELECT FeedID AS feedId, HorseID AS horseId, Grain AS grain, Grass AS grass, Vitamin AS vitamin, Meal AS meal FROM dbo.FEEDINGPLAN WHERE HorseID=? ORDER BY FeedID",
            FeedingPlanDto.class, horseId);
    }

    /**
     * Lấy danh sách nhiệm vụ chăm sóc được giao cho Groom theo ngày, hỗ trợ lọc theo trạng thái hoàn thành.
     */
    public List<CareTaskDto> tasks(int groomId, LocalDate date, Boolean completed, int offset, int size) {
        return db.query(TASK_SELECT + TASK_FILTER + " ORDER BY c.TaskID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            CareTaskDto.class, groomId, date, completed, completed, offset, size);
    }

    /**
     * Đếm tổng số nhiệm vụ chăm sóc theo bộ lọc để phân trang.
     */
    public long countTasks(int groomId, LocalDate date, Boolean completed) {
        return db.count("SELECT COUNT(*) FROM dbo.CARETASK c JOIN dbo.Horse h ON h.HorseID = c.HorseID" + TASK_FILTER,
            groomId, date, completed, completed);
    }

    /**
     * Xem thông tin chi tiết một nhiệm vụ chăm sóc được giao cho Groom.
     */
    public CareTaskDto task(int groomId, int id) {
        return db.queryOne(TASK_SELECT + " WHERE c.TaskID = ? AND c.GroomID = ? AND h.Archived = 0", CareTaskDto.class, id, groomId);
    }

    /**
     * Đánh dấu hoàn thành một nhiệm vụ chăm sóc ([Status] = 1).
     */
    public int complete(int groomId, int id) {
        return db.update("UPDATE dbo.CARETASK SET [Status] = 1 WHERE TaskID = ? AND GroomID = ? AND [Status] = 0", id, groomId);
    }

    /**
     * Xem lịch trình tập luyện / biểu làm việc của Groom với các chú ngựa trong ngày.
     */
    public List<ScheduleDto> schedule(int groomId, LocalDate date) {
        return db.query("SELECT s.ScheduleID AS scheduleId, s.HorseID AS horseId, h.[Name] AS horseName, s.[Date] AS date, s.[Time] AS time FROM dbo.TRAININGSCHEDULE s JOIN dbo.Horse h ON h.HorseID = s.HorseID WHERE s.GroomID = ? AND s.[Date] = ? AND h.Archived = 0 ORDER BY s.[Time], s.ScheduleID",
            ScheduleDto.class, groomId, date);
    }
}
