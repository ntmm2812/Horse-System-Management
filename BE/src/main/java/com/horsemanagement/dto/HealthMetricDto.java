package com.horsemanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HealthMetricDto(
    Long metricId,
    BigDecimal weightKg,
    Short restingHeartRate,
    BigDecimal bodyTemperature,
    Short respiratoryRate,
    LocalDateTime recordedAt,
    String notes
) {}
