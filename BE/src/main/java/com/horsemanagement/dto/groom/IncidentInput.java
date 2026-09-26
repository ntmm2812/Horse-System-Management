package com.horsemanagement.dto.groom;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO dữ liệu đầu vào khi Groom tạo báo cáo sự cố mới:
 * @param horseId      ID chú ngựa gặp sự cố
 * @param incidentType Loại sự cố (Chấn thương, Sốt, Bỏ ăn, v.v.)
 * @param description  Mô tả chi tiết tình trạng
 * @param date         Ngày xảy ra sự cố (chỉ nhận ngày trong quá khứ hoặc hiện tại)
 */
public record IncidentInput(
    @NotNull(message = "Mã ngựa không được để trống")
    @Positive(message = "Mã ngựa phải là số dương")
    Integer horseId,

    @NotBlank(message = "Loại sự cố không được để trống")
    @Size(max = 100, message = "Loại sự cố tối đa 100 ký tự")
    String incidentType,

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    String description,

    @NotNull(message = "Ngày xảy ra không được để trống")
    @PastOrPresent(message = "Ngày xảy ra không thể ở tương lai")
    LocalDate date
) {}
