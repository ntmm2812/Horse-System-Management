package com.horsemanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CompletePreventiveCareRequest(
    @NotNull @Positive Integer performedBy,
    LocalDate completedDate,
    @Size(max = 500) String notes
) {}
