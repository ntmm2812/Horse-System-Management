package com.horsemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateInjuryProgressRequest(
    @Schema(description = "User ID with VETERINARIAN role", example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer loggedBy,
    @Schema(description = "Defaults to current time", example = "2026-10-08T09:00:00")
    LocalDateTime logDate,
    @Schema(minimum = "0", maximum = "100", example = "50")
    @Min(0) @Max(100) Short recoveryPercent,
    @Schema(minimum = "0", maximum = "10", example = "2")
    @Min(0) @Max(10) Short painLevel,
    @Schema(example = "Tiếp tục theo dõi") String notes,
    @Schema(maxLength = 500, example = "https://example.com/injury-photo.jpg")
    @Size(max = 500) String imageUrl
) {}
