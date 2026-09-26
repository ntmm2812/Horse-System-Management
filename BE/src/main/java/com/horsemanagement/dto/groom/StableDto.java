package com.horsemanagement.dto.groom;

/**
 * DTO thông tin ô chuồng và ngựa được phân công cho Groom (dbo.STABLEASSIGNMENT):
 * @param horseId      ID chú ngựa
 * @param horseName    Tên chú ngựa
 * @param stableId     ID chuồng/ô chuồng
 * @param stallNumber  Mã số ô chuồng (ví dụ: "A-01")
 * @param dailyRoutine Quy trình chăm sóc đặc thù hàng ngày
 */
public record StableDto(int horseId, String horseName, Integer stableId, String stallNumber, String dailyRoutine) {}
