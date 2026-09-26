package com.horsemanagement.dto.groom;

import java.time.LocalDate;

/**
 * DTO hiển thị thông tin chi tiết một báo cáo sự cố (dbo.INCIDENTREPORT):
 * @param reportId     ID báo cáo
 * @param horseId      ID chú ngựa gặp sự cố
 * @param horseName    Tên chú ngựa
 * @param groomId      ID người báo cáo (Groom)
 * @param incidentType Loại sự cố (Chấn thương, Sốt, Bỏ ăn, Hành vi bất thường, v.v.)
 * @param description  Mô tả chi tiết sự việc
 * @param date         Ngày xảy ra sự cố
 * @param imageUrl     Đường dẫn tải ảnh đính kèm (URL API)
 */
public record IncidentDto(int reportId, int horseId, String horseName, int groomId,
                         String incidentType, String description, LocalDate date, String imageUrl) {}
