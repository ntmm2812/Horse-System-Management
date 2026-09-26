package com.horsemanagement.dto.manager;

/**
 * DTO phản hồi ID của bản ghi tài chính vừa được tạo thành công:
 * @param entryId ID bút toán tài chính (dbo.FinanceEntry)
 */
public record CreatedEntryDto(int entryId) {}
