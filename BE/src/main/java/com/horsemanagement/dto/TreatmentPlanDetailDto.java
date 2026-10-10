package com.horsemanagement.dto;

import com.horsemanagement.entity.TreatmentPlan;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TreatmentPlanDetailDto(
    Integer treatmentId,
    Integer recordId,
    Integer horseId,
    String horseName,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    TreatmentPlan.Status status,
    LocalDateTime createdAt,
    List<PrescriptionItemDto> prescriptions
) {}
