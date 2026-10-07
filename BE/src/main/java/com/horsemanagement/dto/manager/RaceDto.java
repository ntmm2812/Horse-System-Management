package com.horsemanagement.dto.manager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ============================================================
 * FILE: RaceDto.java — Nhóm DTO liên quan đến Cuộc đua & Kết quả
 * MỤC ĐÍCH:
 *   - RaceDto        : Thông tin 1 cuộc đua (trả về danh sách / chi tiết)
 *   - RaceResultDto  : Kết quả của 1 ngựa trong 1 cuộc đua
 *   - RaceWithResultsDto : Cuộc đua kèm toàn bộ kết quả (dùng cho xem chi tiết)
 * ============================================================
 */
public final class RaceDto {

    // DTO trả về thông tin 1 cuộc đua
    public record RaceInfo(
            int raceId,
            String raceName,
            LocalDate date,
            LocalTime time,
            String venue,            // Địa điểm tổ chức (nullable)
            Integer distanceMeters,  // Cự ly đua mét (nullable)
            int registrationCount,   // Số ngựa đã đăng ký
            boolean hasResults       // true nếu đã có kết quả nhập vào
    ) {}

    // DTO kết quả 1 ngựa trong 1 cuộc đua
    public record RaceResultDto(
            int horseId,
            String horseName,
            LocalDate registrationDate,
            Integer position,       // Vị trí xếp hạng; null = chưa có kết quả
            String finishTime,      // Thời gian về đích dạng "mm:ss.sss"; null = chưa có
            java.math.BigDecimal prizeMoney  // Tiền thưởng VNĐ; null = không được thưởng
    ) {}

    // DTO cuộc đua kèm danh sách kết quả toàn bộ ngựa tham gia
    public record RaceWithResultsDto(
            RaceInfo race,
            List<RaceResultDto> results
    ) {}
}
