package com.horsemanagement.dto.manager;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO chi tiết một bút toán thu chi tài chính (dbo.FinanceEntry):
 * @param entryId     ID bút toán
 * @param horseId     ID chú ngựa liên quan (nếu có)
 * @param entryType   Loại giao dịch (INCOME hoặc EXPENSE)
 * @param amount      Số tiền giao dịch
 * @param category    Danh mục tài chính
 * @param description Mô tả nội dung khoản thu/chi
 * @param entryDate   Ngày ghi nhận giao dịch
 * @param createdBy   ID người tạo bút toán
 */
public record FinanceEntryDto(int entryId, Integer horseId, String entryType, BigDecimal amount,
                             String category, String description, LocalDate entryDate, int createdBy) {}
