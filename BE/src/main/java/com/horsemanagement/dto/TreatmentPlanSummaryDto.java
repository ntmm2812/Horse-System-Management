package com.horsemanagement.dto;

import com.horsemanagement.entity.TreatmentPlan;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TreatmentPlanSummaryDto(
    Integer treatmentId,
    Integer recordId,
    Integer horseId,
    String horseName,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    TreatmentPlan.Status status,
    LocalDateTime createdAt
) {}
