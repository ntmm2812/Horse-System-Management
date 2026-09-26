package com.horsemanagement.dao.support;

import com.horsemanagement.exception.ApiException;

import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Lớp hỗ trợ truy vấn cơ sở dữ liệu dùng chung (Database Helper):
 * - Bọc lại Spring JdbcTemplate để chuẩn hóa việc thực thi SQL.
 * - Luôn sử dụng Parameterized Query (dấu '?') để ngăn chặn hoàn toàn tấn công SQL Injection.
 * - Hỗ trợ ánh xạ trực tiếp sang Java Record / DataClass (DataClassRowMapper).
 * - Tự động chuyển đổi các kiểu dữ liệu ngày giờ của SQL Server (DATE, TIME, TIMESTAMP) sang Java 8 Time.
 */
@Repository
public class Database {

    private final JdbcTemplate jdbc;

    public Database(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Thực hiện truy vấn trả về danh sách các đối tượng kiểu T.
     */
    public <T> List<T> query(String sql, Class<T> type, Object... args) {
        return jdbc.query(sql, new org.springframework.jdbc.core.DataClassRowMapper<>(type), args);
    }

    /**
     * Thực hiện truy vấn trả về duy nhất 1 đối tượng kiểu T, ném lỗi 404 nếu không tìm thấy.
     */
    public <T> T queryOne(String sql, Class<T> type, Object... args) {
        var rows = query(sql, type, args);
        if (rows.isEmpty()) {
            throw ApiException.notFound();
        }
        return rows.get(0);
    }

    /**
     * Thực hiện truy vấn trả về danh sách dạng Map (Key-Value), xử lý chuẩn hóa ngày giờ.
     */
    public List<Map<String, Object>> list(String sql, Object... args) {
        var rows = jdbc.queryForList(sql, args);
        // DATE/TIME/DATETIME2 không chứa múi giờ: chuyển thành LocalDate, LocalTime, LocalDateTime
        rows.forEach(row -> row.replaceAll((key, value) -> {
            if (value instanceof java.sql.Date date) return date.toLocalDate();
            if (value instanceof java.sql.Time time) return time.toLocalTime();
            if (value instanceof java.sql.Timestamp time) return time.toLocalDateTime();
            return value;
        }));
        return rows;
    }

    /**
     * Thực hiện truy vấn trả về 1 dòng dạng Map, ném lỗi 404 nếu không tìm thấy.
     */
    public Map<String, Object> one(String sql, Object... args) {
        var rows = list(sql, args);
        if (rows.isEmpty()) {
            throw ApiException.notFound();
        }
        return rows.get(0);
    }

    /**
     * Thực hiện lệnh UPDATE hoặc DELETE và trả về số dòng bị ảnh hưởng.
     */
    public int update(String sql, Object... args) {
        return jdbc.update(sql, args);
    }

    /**
     * Thực hiện câu lệnh COUNT(*) trả về tổng số bản ghi.
     */
    public long count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    /**
     * Thực hiện câu lệnh INSERT và tự động lấy ID tăng tự động (IDENTITY / Auto Increment) được tạo ra.
     */
    public int insert(String sql, Object... args) {
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement;
        }, key);
        return Objects.requireNonNull(key.getKey()).intValue();
    }

    /** Trích xuất giá trị int từ map kết quả */
    public static int id(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).intValue();
    }

    /** Trích xuất giá trị String từ map kết quả */
    public static String text(Map<String, Object> row, String column) {
        return (String) row.get(column);
    }

    /** Trích xuất giá trị boolean từ map kết quả */
    public static boolean flag(Map<String, Object> row, String column) {
        return Boolean.TRUE.equals(row.get(column));
    }

    /** Kiểm tra và tính toán offset cho phân trang */
    public static int offset(int page, int size) {
        if (page < 0 || page > 100000 || size < 1 || size > 100) {
            throw new ApiException(400, "page phải từ 0 đến 100000; size từ 1 đến 100.");
        }
        return page * size;
    }
}
