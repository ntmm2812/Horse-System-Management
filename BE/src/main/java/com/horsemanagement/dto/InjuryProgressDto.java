package com.horsemanagement.dto;

import java.time.LocalDateTime;

public record InjuryProgressDto(
    Integer progressId, Integer injuryId, Integer loggedBy, String loggedByName,
    LocalDateTime logDate, Short recoveryPercent, Short painLevel,
    String notes, String imageUrl
) {}
