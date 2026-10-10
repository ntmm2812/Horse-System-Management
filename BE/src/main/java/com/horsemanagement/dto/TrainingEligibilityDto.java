package com.horsemanagement.dto;

import com.horsemanagement.entity.Horse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TrainingEligibilityDto(
    Integer horseId, String horseName,
    Horse.HealthStatus healthStatus, Horse.ReadinessStatus readinessStatus,
    boolean isTrainingLocked,
    List<TrainingLockDto> activeLocks,
    @Schema(description = "Restrictions caused only by ACTIVE training locks; not medical clearance")
    LockRestrictions restrictedByLocks
) {
    public record LockRestrictions(boolean light, boolean moderate, boolean heavy) {}
}
