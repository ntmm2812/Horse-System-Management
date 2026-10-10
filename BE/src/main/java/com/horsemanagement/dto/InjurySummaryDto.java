package com.horsemanagement.dto;

import com.horsemanagement.entity.Injury;
import java.time.LocalDate;

public record InjurySummaryDto(
    Integer injuryId, Integer recordId, Integer horseId, String horseName,
    String bodyPart, Injury.BodySystem bodySystem, Injury.Severity severity,
    Injury.Status status, LocalDate occurredDate, LocalDate healedDate
) {}
