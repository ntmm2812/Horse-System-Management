package com.horsemanagement.dto;

import com.horsemanagement.entity.Injury;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInjuryRequest(
    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive Integer recordId,
    @Schema(example = "Chân trước trái", maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Size(max = 100) String bodyPart,
    @Schema(description = "MUSCLE, BONE, TENDON, LIGAMENT, HOOF, SKIN, OTHER",
        example = "MUSCLE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull Injury.BodySystem bodySystem,
    @Schema(example = "front_left_leg", maxLength = 100)
    @Size(max = 100) String modelMeshId,
    @Schema(description = "DECIMAL(9,4)", example = "0.2000")
    @Digits(integer = 5, fraction = 4) BigDecimal positionX,
    @Schema(description = "DECIMAL(9,4)", example = "0.5000")
    @Digits(integer = 5, fraction = 4) BigDecimal positionY,
    @Schema(description = "DECIMAL(9,4)", example = "0.1000")
    @Digits(integer = 5, fraction = 4) BigDecimal positionZ,
    @Schema(example = "Chấn thương phần mềm", maxLength = 100)
    @Size(max = 100) String injuryType,
    @Schema(description = "MINOR, MODERATE, SEVERE", example = "MODERATE",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull Injury.Severity severity,
    @Schema(example = "2026-10-03") LocalDate occurredDate,
    @Schema(example = "Theo dõi phục hồi") String description
) {}
