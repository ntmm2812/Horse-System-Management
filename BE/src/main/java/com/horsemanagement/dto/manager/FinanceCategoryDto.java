package com.horsemanagement.dto.manager;

import java.math.BigDecimal;

/**
 * DTO tổng hợp tài chính theo từng danh mục:
 * @param entryType Loại giao dịch (INCOME hoặc EXPENSE)
 * @param category  Tên danh mục (ví dụ: Thức ăn, Thuốc men, Phí chuồng, Lương...)
 * @param amount    Tổng số tiền
 */
public record FinanceCategoryDto(String entryType, String category, BigDecimal amount) {}
