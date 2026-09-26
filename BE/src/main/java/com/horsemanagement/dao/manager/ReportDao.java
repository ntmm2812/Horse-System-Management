package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.manager.Requests.FinanceInput;
import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) trích xuất dữ liệu báo cáo và tài chính:
 * - Thống kê hiệu suất tập luyện và chăm sóc ngựa (PerformanceDto: tốc độ trung bình, nhịp tim, số bài tập, tỷ lệ hoàn thành việc chăm sóc).
 * - Tổng hợp doanh thu, chi phí, lợi nhuận ròng trong khoảng thời gian.
 * - Phân tích chi tiết tài chính theo từng danh mục (FinanceCategoryDto).
 * - Lưu trữ và phân trang danh sách các bút toán tài chính (dbo.FinanceEntry).
 */
@Repository
public class ReportDao {

    private final Database db;

    public ReportDao(Database db) {
        this.db = db;
    }

    /**
     * DTO tổng hợp số liệu tài chính: tổng thu, tổng chi, lợi nhuận ròng, số bút toán.
     */
    public record FinanceTotals(BigDecimal income, BigDecimal expense, BigDecimal net, long entryCount) {}

    /**
     * Thống kê hiệu suất rèn luyện và chăm sóc của các chú ngựa trong khoảng thời gian xác định.
     */
    public List<PerformanceDto> performance(LocalDate from, LocalDate to, int offset, int size) {
        return db.query("""
            SELECT h.HorseID AS horseId, h.[Name] AS horseName,
                COUNT(s.SessionID) AS sessionCount, AVG(s.Speed) AS averageSpeed,
                AVG(CAST(s.HeartRate AS DECIMAL(10,2))) AS averageHeartRate,
                (SELECT COUNT(*) FROM dbo.CARETASK c WHERE c.HorseID = h.HorseID AND c.[Date] BETWEEN ? AND ?) AS careTaskCount,
                (SELECT COUNT(*) FROM dbo.CARETASK c WHERE c.HorseID = h.HorseID AND c.[Date] BETWEEN ? AND ? AND c.[Status] = 1) AS completedTaskCount
            FROM dbo.Horse h
            LEFT JOIN dbo.TRAININGSESSION s ON s.HorseID = h.HorseID AND s.[Date] BETWEEN ? AND ?
            WHERE h.Archived = 0
            GROUP BY h.HorseID, h.[Name]
            ORDER BY h.HorseID
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """, PerformanceDto.class, from, to, from, to, from, to, offset, size);
    }

    /**
     * Đếm tổng số ngựa còn hoạt động để phân trang báo cáo hiệu suất.
     */
    public long countActiveHorses() {
        return db.count("SELECT COUNT(*) FROM dbo.Horse WHERE Archived=0");
    }

    /**
     * Tổng hợp tổng thu, tổng chi và số dư ròng trong một khoảng thời gian.
     */
    public FinanceTotals finance(LocalDate from, LocalDate to) {
        return db.queryOne("""
            SELECT COALESCE(SUM(CASE WHEN EntryType='INCOME' THEN Amount ELSE 0 END), 0) AS income,
                COALESCE(SUM(CASE WHEN EntryType='EXPENSE' THEN Amount ELSE 0 END), 0) AS expense,
                COALESCE(SUM(CASE WHEN EntryType='INCOME' THEN Amount ELSE -Amount END), 0) AS net,
                COUNT(*) AS entryCount
            FROM dbo.FinanceEntry
            WHERE EntryDate BETWEEN ? AND ?
            """, FinanceTotals.class, from, to);
    }

    /**
     * Thống kê tổng số tiền theo từng nhóm/danh mục thu chi.
     */
    public List<FinanceCategoryDto> categories(LocalDate from, LocalDate to) {
        return db.query("""
            SELECT EntryType AS entryType, Category AS category, SUM(Amount) AS amount
            FROM dbo.FinanceEntry
            WHERE EntryDate BETWEEN ? AND ?
            GROUP BY EntryType, Category
            ORDER BY EntryType, Category
            """, FinanceCategoryDto.class, from, to);
    }

    /**
     * Lấy danh sách phân trang các bút toán thu chi tài chính theo thời gian giảm dần.
     */
    public List<FinanceEntryDto> entries(LocalDate from, LocalDate to, int offset, int size) {
        return db.query("""
            SELECT EntryID AS entryId, HorseID AS horseId, EntryType AS entryType, Amount AS amount,
                Category AS category, [Description] AS description, EntryDate AS entryDate, CreatedBy AS createdBy
            FROM dbo.FinanceEntry
            WHERE EntryDate BETWEEN ? AND ?
            ORDER BY EntryDate DESC, EntryID DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """, FinanceEntryDto.class, from, to, offset, size);
    }

    /**
     * Đếm tổng số bút toán trong khoảng thời gian để phân trang.
     */
    public long countEntries(LocalDate from, LocalDate to) {
        return db.count("SELECT COUNT(*) FROM dbo.FinanceEntry WHERE EntryDate BETWEEN ? AND ?", from, to);
    }

    /**
     * Lưu một bút toán tài chính mới vào cơ sở dữ liệu.
     */
    public int insert(int userId, FinanceInput input) {
        return db.insert("""
            INSERT INTO dbo.FinanceEntry (HorseID, EntryType, Amount, Category, [Description], EntryDate, CreatedBy)
            VALUES (?,?,?,?,?,?,?)
            """, input.horseId(), input.entryType().name(), input.amount(), input.category(), input.description(), input.entryDate(), userId);
    }
}
