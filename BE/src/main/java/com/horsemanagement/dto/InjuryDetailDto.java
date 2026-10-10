package com.horsemanagement.dto;

import com.horsemanagement.entity.Injury;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InjuryDetailDto(
    Integer injuryId, Integer recordId, Integer horseId, String horseName,
    String bodyPart, Injury.BodySystem bodySystem, String modelMeshId,
    BigDecimal positionX, BigDecimal positionY, BigDecimal positionZ,
    String injuryType, Injury.Severity severity, Injury.Status status,
    LocalDate occurredDate, LocalDate healedDate, String description
) {}
