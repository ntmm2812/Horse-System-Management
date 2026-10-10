package com.horsemanagement.dto;

import com.horsemanagement.entity.PreventiveCareSchedule;
import java.time.LocalDate;

public record PreventiveCareDto(
    Integer scheduleId, Integer horseId, String horseName,
    PreventiveCareSchedule.CareType careType, String description, LocalDate dueDate,
    Short intervalDays, Short remindBeforeDays, PreventiveCareSchedule.Status status,
    LocalDate completedDate, Integer performedBy, String performedByName,
    String notes, boolean pendingPastDue
) {}
