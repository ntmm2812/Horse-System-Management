package com.horsemanagement.dto;

import com.horsemanagement.entity.Horse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateMedicalRecordRequest(
    @Schema(description = "Existing horse ID", example = "3", minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer horseId,
    @Schema(description = "Existing user ID with role_code VETERINARIAN; temporary until authentication",
        example = "3", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer vetId,
    @Schema(description = "Optional incident ID; must belong to this horse and not be linked already",
        example = "1", minimum = "1")
    @Positive Integer incidentId,
    @Schema(description = "Examination date and time; defaults to application current local time",
        example = "2026-10-10T09:00:00", type = "string", format = "date-time")
    LocalDateTime examDate,
    @Schema(description = "Reason for examination; maximum 255 characters",
        example = "Routine examination", maxLength = 255)
    @Size(max = 255) String reason,
    @Schema(description = "Observed symptoms; SQL Server NVARCHAR(MAX)", example = "Normal appetite")
    String symptoms,
    @Schema(description = "Diagnosis; SQL Server NVARCHAR(MAX)", example = "No acute concern")
    String diagnosis,
    @Schema(description = "New horse health status. Does not change readiness or training lock",
        example = "MONITORING", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull Horse.HealthStatus healthStatusAfter,
    @Schema(description = "Additional medical notes; SQL Server NVARCHAR(MAX)",
        example = "Follow up next week")
    String notes
) {}
