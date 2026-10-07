package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.RaceDto.*;
import com.horsemanagement.dto.manager.Requests.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * FILE: RaceDao.java — DAO truy vấn bảng RACE & RACEREGISTRATION
 * MỤC ĐÍCH:
 *   - Cung cấp CRUD đầy đủ cho cuộc đua (RACE)
 *   - Đăng ký ngựa tham gia (RACEREGISTRATION)
 *   - Nhập & cập nhật kết quả thi đấu (Position, FinishTime, PrizeMoney)
 * BẢNG DÙNG: dbo.RACE, dbo.RACEREGISTRATION, dbo.Horse
 * ============================================================
 */
@Repository
public class RaceDao {

    private final Database db;

    public RaceDao(Database db) {
        this.db = db;
    }

    // ──────────────────────────────────────────────
    // PHẦN 1: QUẢN LÝ CUỘC ĐUA (RACE CRUD)
    // ──────────────────────────────────────────────

    /**
     * Lấy danh sách tất cả cuộc đua kèm số ngựa đăng ký và cờ đã có kết quả chưa.
     * Sắp xếp theo ngày gần nhất trước.
     */
    public List<RaceInfo> listRaces() {
        String sql = """
            SELECT r.RaceID, r.RaceName, r.[Date], r.[Time],
                   r.Venue, r.DistanceMeters,
                   COUNT(rr.HorseID) AS RegistrationCount,
                   CAST(MAX(CASE WHEN rr.[Position] IS NOT NULL THEN 1 ELSE 0 END) AS BIT) AS HasResults
            FROM dbo.RACE r
            LEFT JOIN dbo.RACEREGISTRATION rr ON rr.RaceID = r.RaceID
            GROUP BY r.RaceID, r.RaceName, r.[Date], r.[Time], r.Venue, r.DistanceMeters
            ORDER BY r.[Date] DESC, r.[Time] DESC
            """;
        return db.list(sql, rs -> new RaceInfo(
                rs.getInt("RaceID"),
                rs.getString("RaceName"),
                rs.getDate("Date").toLocalDate(),
                rs.getTime("Time").toLocalTime(),
                rs.getString("Venue"),
                (Integer) rs.getObject("DistanceMeters"),
                rs.getInt("RegistrationCount"),
                rs.getBoolean("HasResults")
        ));
    }

    /**
     * Xem chi tiết 1 cuộc đua kèm toàn bộ danh sách kết quả của từng ngựa.
     * Trả về Optional.empty() nếu không tồn tại RaceID.
     */
    public Optional<RaceWithResultsDto> raceWithResults(int raceId) {
        // Bước 1: lấy thông tin cuộc đua
        String raceSql = """
            SELECT r.RaceID, r.RaceName, r.[Date], r.[Time],
                   r.Venue, r.DistanceMeters,
                   COUNT(rr.HorseID) AS RegistrationCount,
                   CAST(MAX(CASE WHEN rr.[Position] IS NOT NULL THEN 1 ELSE 0 END) AS BIT) AS HasResults
            FROM dbo.RACE r
            LEFT JOIN dbo.RACEREGISTRATION rr ON rr.RaceID = r.RaceID
            WHERE r.RaceID = ?
            GROUP BY r.RaceID, r.RaceName, r.[Date], r.[Time], r.Venue, r.DistanceMeters
            """;
        Optional<RaceInfo> raceOpt = db.one(raceSql, rs -> new RaceInfo(
                rs.getInt("RaceID"), rs.getString("RaceName"),
                rs.getDate("Date").toLocalDate(), rs.getTime("Time").toLocalTime(),
                rs.getString("Venue"), (Integer) rs.getObject("DistanceMeters"),
                rs.getInt("RegistrationCount"), rs.getBoolean("HasResults")
        ), raceId);

        if (raceOpt.isEmpty()) return Optional.empty();

        // Bước 2: lấy danh sách kết quả từng ngựa trong cuộc đua đó
        String resultSql = """
            SELECT rr.HorseID, h.[Name] AS HorseName,
                   rr.RegistrationDate, rr.[Position], rr.FinishTime, rr.PrizeMoney
            FROM dbo.RACEREGISTRATION rr
            JOIN dbo.Horse h ON h.HorseID = rr.HorseID
            WHERE rr.RaceID = ?
            ORDER BY rr.[Position] ASC, rr.HorseID ASC
            """;
        List<RaceResultDto> results = db.list(resultSql, rs -> new RaceResultDto(
                rs.getInt("HorseID"),
                rs.getString("HorseName"),
                rs.getDate("RegistrationDate").toLocalDate(),
                (Integer) rs.getObject("Position"),
                rs.getString("FinishTime"),
                rs.getBigDecimal("PrizeMoney")
        ), raceId);

        return Optional.of(new RaceWithResultsDto(raceOpt.get(), results));
    }

    /**
     * Tạo mới một cuộc đua.
     * Trả về RaceID vừa được tạo.
     */
    public int createRace(RaceInput input) {
        String sql = """
            INSERT INTO dbo.RACE (RaceName, [Date], [Time], Venue, DistanceMeters)
            OUTPUT INSERTED.RaceID
            VALUES (?, ?, ?, ?, ?)
            """;
        return db.insert(sql,
                input.raceName(), input.date(), input.time(),
                input.venue(), input.distanceMeters());
    }

    /**
     * Cập nhật thông tin cuộc đua (chỉ cho phép trước khi có kết quả).
     * Trả về số dòng bị ảnh hưởng (0 nếu không tìm thấy RaceID).
     */
    public int updateRace(int raceId, RaceInput input) {
        String sql = """
            UPDATE dbo.RACE
            SET RaceName = ?, [Date] = ?, [Time] = ?, Venue = ?, DistanceMeters = ?
            WHERE RaceID = ?
            """;
        return db.update(sql,
                input.raceName(), input.date(), input.time(),
                input.venue(), input.distanceMeters(), raceId);
    }

    /**
     * Xóa cuộc đua (chỉ cho phép nếu chưa có kết quả nào được nhập).
     */
    public int deleteRace(int raceId) {
        return db.update("DELETE FROM dbo.RACE WHERE RaceID = ?", raceId);
    }

    // ──────────────────────────────────────────────
    // PHẦN 2: ĐĂNG KÝ NGỰA THAM GIA (REGISTRATION)
    // ──────────────────────────────────────────────

    /**
     * Đăng ký 1 ngựa vào 1 cuộc đua.
     * Tự động dùng ngày hiện tại làm RegistrationDate.
     */
    public void registerHorse(int raceId, int horseId) {
        String sql = """
            IF NOT EXISTS (SELECT 1 FROM dbo.RACEREGISTRATION WHERE HorseID = ? AND RaceID = ?)
                INSERT INTO dbo.RACEREGISTRATION (HorseID, RaceID, RegistrationDate)
                VALUES (?, ?, CAST(GETDATE() AS DATE))
            """;
        db.update(sql, horseId, raceId, horseId, raceId);
    }

    /**
     * Hủy đăng ký ngựa khỏi cuộc đua (chỉ nếu chưa có kết quả).
     */
    public int cancelRegistration(int raceId, int horseId) {
        String sql = """
            DELETE FROM dbo.RACEREGISTRATION
            WHERE HorseID = ? AND RaceID = ? AND [Position] IS NULL
            """;
        return db.update(sql, horseId, raceId);
    }

    // ──────────────────────────────────────────────
    // PHẦN 3: NHẬP / CẬP NHẬT KẾT QUẢ THI ĐẤU
    // ──────────────────────────────────────────────

    /**
     * Nhập hoặc cập nhật kết quả thi đấu của 1 ngựa trong 1 cuộc đua.
     * - Position   : vị trí xếp hạng (1 = nhất, bắt buộc)
     * - FinishTime : thời gian về đích dạng 'mm:ss.sss' (tùy chọn)
     * - PrizeMoney : tiền thưởng VNĐ (tùy chọn)
     * Trả về số dòng bị ảnh hưởng (0 nếu cặp HorseID-RaceID không tồn tại).
     */
    public int upsertResult(int raceId, int horseId, RaceResultInput input) {
        String sql = """
            UPDATE dbo.RACEREGISTRATION
            SET [Position] = ?, FinishTime = ?, PrizeMoney = ?
            WHERE RaceID = ? AND HorseID = ?
            """;
        return db.update(sql,
                input.position(), input.finishTime(), input.prizeMoney(),
                raceId, horseId);
    }

    /**
     * Xóa kết quả (đặt lại về NULL) của 1 ngựa trong 1 cuộc đua.
     * Dùng khi nhập sai và cần sửa lại.
     */
    public int clearResult(int raceId, int horseId) {
        String sql = """
            UPDATE dbo.RACEREGISTRATION
            SET [Position] = NULL, FinishTime = NULL, PrizeMoney = NULL
            WHERE RaceID = ? AND HorseID = ?
            """;
        return db.update(sql, raceId, horseId);
    }

    /**
     * Kiểm tra xem cặp (HorseID, RaceID) có tồn tại không.
     * Dùng để validate trước khi nhập kết quả.
     */
    public boolean registrationExists(int raceId, int horseId) {
        String sql = "SELECT COUNT(1) FROM dbo.RACEREGISTRATION WHERE RaceID = ? AND HorseID = ?";
        return db.one(sql, rs -> rs.getInt(1) > 0, raceId, horseId).orElse(false);
    }

    /**
     * Kiểm tra cuộc đua có tồn tại không.
     */
    public boolean raceExists(int raceId) {
        return db.one("SELECT COUNT(1) FROM dbo.RACE WHERE RaceID = ?",
                rs -> rs.getInt(1) > 0, raceId).orElse(false);
    }
}
