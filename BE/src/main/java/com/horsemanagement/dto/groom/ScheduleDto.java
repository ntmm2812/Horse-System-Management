package com.horsemanagement.dto.groom;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO lịch trình huấn luyện / hoạt động của ngựa (dbo.TRAININGSCHEDULE):
 * @param scheduleId ID lịch trình
 * @param horseId    ID chú ngựa
 * @param horseName  Tên chú ngựa
 * @param date       Ngày thực hiện
 * @param time       Giờ thực hiện
 */
public record ScheduleDto(int scheduleId, int horseId, String horseName, LocalDate date, LocalTime time) {}
