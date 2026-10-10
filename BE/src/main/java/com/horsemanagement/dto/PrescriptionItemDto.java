package com.horsemanagement.dto;

import com.horsemanagement.entity.PrescriptionItem;

public record PrescriptionItemDto(
    Integer prescriptionItemId,
    Integer treatmentId,
    Integer supplyId,
    String supplyName,
    String dosage,
    String frequency,
    Short durationDays,
    PrescriptionItem.Route route,
    String instructions
) {}
