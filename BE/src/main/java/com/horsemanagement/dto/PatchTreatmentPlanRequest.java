package com.horsemanagement.dto;

import com.horsemanagement.entity.TreatmentPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record PatchTreatmentPlanRequest(
    @Schema(description = "New description; omit or null to retain the current value",
        example = "Continue rest and monitoring")
    String description,
    @Schema(description = "New end date; omit or null to retain the current value. Clearing is not supported",
        example = "2026-10-24")
    LocalDate endDate,
    @Schema(description = "ACTIVE, COMPLETED or CANCELLED; omit or null to retain the current value",
        example = "COMPLETED")
    TreatmentPlan.Status status
) {}
