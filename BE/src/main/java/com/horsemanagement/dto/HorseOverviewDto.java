package com.horsemanagement.dto;

import com.horsemanagement.entity.Horse;

public record HorseOverviewDto(
    Integer horseId,
    String name,
    String breed,
    Horse.Gender gender,
    Horse.HealthStatus healthStatus,
    Horse.ReadinessStatus readinessStatus,
    boolean trainingLocked,
    String ownerName,
    String managerName
) {}
