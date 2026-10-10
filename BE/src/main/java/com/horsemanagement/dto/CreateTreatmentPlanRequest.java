package com.horsemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateTreatmentPlanRequest(
    @Schema(description = "Existing medical record ID", example = "1", minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer recordId,
    @Schema(description = "Treatment plan description (NVARCHAR(MAX))",
        example = "Rest and monitor progress", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String description,
    @Schema(description = "Start date, ISO yyyy-MM-dd", example = "2026-10-03",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull LocalDate startDate,
    @Schema(description = "Optional end date; must be on or after startDate", example = "2026-10-17")
    LocalDate endDate
) {}
