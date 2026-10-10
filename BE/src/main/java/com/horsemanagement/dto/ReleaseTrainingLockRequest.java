package com.horsemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReleaseTrainingLockRequest(
    @Schema(description = "Existing VETERINARIAN user ID; temporary until authentication exists",
        example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer releasedBy
) {}
