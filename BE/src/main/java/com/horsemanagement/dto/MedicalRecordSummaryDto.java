package com.horsemanagement.dto;

import com.horsemanagement.entity.Horse;
import java.time.LocalDateTime;

public record MedicalRecordSummaryDto(
    Integer recordId,
    Integer horseId,
    String horseName,
    Integer vetId,
    String vetName,
    LocalDateTime examDate,
    String reason,
    Horse.HealthStatus healthStatusAfter
) {}
