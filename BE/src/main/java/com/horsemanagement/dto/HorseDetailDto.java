package com.horsemanagement.dto;

import com.horsemanagement.entity.Horse;
import java.math.BigDecimal;
import java.time.LocalDate;

public record HorseDetailDto(
    Integer horseId,
    String name,
    String registrationNo,
    String microchipNo,
    String breed,
    Horse.Gender gender,
    String color,
    LocalDate dateOfBirth,
    String countryOfOrigin,
    BigDecimal heightCm,
    BigDecimal currentWeightKg,
    Horse.HealthStatus healthStatus,
    Horse.ReadinessStatus readinessStatus,
    boolean trainingLocked,
    Horse.Status status,
    String photoUrl,
    String ownerName,
    String managerName,
    HealthMetricDto latestHealthMetric
) {}
