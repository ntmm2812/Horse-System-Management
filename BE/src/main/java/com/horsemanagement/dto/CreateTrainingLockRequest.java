package com.horsemanagement.dto;

import com.horsemanagement.entity.TrainingLock;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateTrainingLockRequest(
    @Schema(example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer horseId,
    @Schema(description = "Existing VETERINARIAN user ID; temporary until authentication exists",
        example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer lockedBy,
    @Schema(description = "FULL or HEAVY_ONLY", example = "FULL",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull TrainingLock.LockLevel lockLevel,
    @Schema(maxLength = 500, example = "Tạm nghỉ để theo dõi hồi phục",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(max = 500) String reason,
    @Schema(description = "Optional injury belonging to the same horse", example = "1")
    @Positive Integer injuryId,
    @Schema(description = "Optional expected end; it never releases the lock automatically",
        example = "2026-10-17T08:30:00")
    LocalDateTime expectedEndAt
) {}
