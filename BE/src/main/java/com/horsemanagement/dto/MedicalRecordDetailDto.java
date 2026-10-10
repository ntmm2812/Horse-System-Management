package com.horsemanagement.dto;

import com.horsemanagement.entity.Horse;
import java.time.LocalDateTime;

public record MedicalRecordDetailDto(
    Integer recordId,
    Integer horseId,
    String horseName,
    Integer vetId,
    String vetName,
    Integer incidentId,
    LocalDateTime examDate,
    String reason,
    String symptoms,
    String diagnosis,
    Horse.HealthStatus healthStatusAfter,
    String notes
) {}
