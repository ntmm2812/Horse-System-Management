package com.horsemanagement.dto;

import com.horsemanagement.entity.PreventiveCareSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePreventiveCareRequest(
    @NotNull @Positive Integer horseId,
    @NotNull PreventiveCareSchedule.CareType careType,
    @Size(max = 255) String description,
    @NotNull LocalDate dueDate,
    @Positive Short intervalDays,
    @Schema(description = "0..255; defaults to 3") @Min(0) @Max(255) Short remindBeforeDays,
    @Size(max = 500) String notes
) {}
