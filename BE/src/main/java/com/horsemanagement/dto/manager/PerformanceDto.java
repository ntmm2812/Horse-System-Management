package com.horsemanagement.dto.manager;

import java.math.BigDecimal;

/**
 * DTO báo cáo hiệu suất rèn luyện và chăm sóc ngựa:
 * @param horseId            ID chú ngựa
 * @param horseName          Tên chú ngựa
 * @param sessionCount       Tổng số buổi huấn luyện/luyện tập
 * @param averageSpeed       Tốc độ chạy trung bình (km/h)
 * @param averageHeartRate   Nhịp tim trung bình (bpm)
 * @param careTaskCount      Tổng số công việc chăm sóc được giao
 * @param completedTaskCount Số công việc chăm sóc đã hoàn thành
 */
public record PerformanceDto(int horseId, String horseName, long sessionCount,
                            BigDecimal averageSpeed, BigDecimal averageHeartRate,
                            long careTaskCount, long completedTaskCount) {}
