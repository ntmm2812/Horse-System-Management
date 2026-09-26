package com.horsemanagement.dto.manager;

import java.time.LocalDateTime;

/**
 * DTO nhật ký kiểm toán (dbo.AuditLog):
 * @param logId       ID bản ghi nhật ký
 * @param userId      ID người dùng thực hiện
 * @param userName    Tên người dùng
 * @param action      Hành động (CREATE, UPDATE, DELETE, v.v.)
 * @param targetTable Bảng dữ liệu bị tác động
 * @param targetId    ID đối tượng bị tác động
 * @param timestamp   Thời điểm thực hiện
 */
public record AuditLogDto(int logId, int userId, String userName, String action, String targetTable, int targetId, LocalDateTime timestamp) {}
