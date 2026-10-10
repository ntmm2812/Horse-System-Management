package com.horsemanagement.dto;

import com.horsemanagement.entity.PrescriptionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePrescriptionItemRequest(
    @Schema(description = "Existing MEDICINE supply ID", example = "4", minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer supplyId,
    @Schema(description = "Dosage text, maximum 100 characters; no dosage advice is generated",
        example = "As directed by veterinarian", maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(max = 100) String dosage,
    @Schema(description = "Frequency text, maximum 100 characters", example = "Per written care schedule",
        maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(max = 100) String frequency,
    @Schema(description = "Optional positive number of days (SQL SMALLINT)", example = "7",
        minimum = "1")
    @Positive Short durationDays,
    @Schema(description = "Optional route: ORAL, INJECTION, TOPICAL, IV or OTHER", example = "ORAL")
    PrescriptionItem.Route route,
    @Schema(description = "Optional instructions, maximum 255 characters", maxLength = 255,
        example = "Follow veterinarian instructions")
    @Size(max = 255) String instructions
) {}
