package com.horsemanagement.mapper;

import com.horsemanagement.dto.PreventiveCareDto;
import com.horsemanagement.entity.PreventiveCareSchedule;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class PreventiveCareMapper {
    public PreventiveCareDto toDto(PreventiveCareSchedule schedule, LocalDate today) {
        var performer = schedule.getPerformedBy();
        return new PreventiveCareDto(schedule.getScheduleId(), schedule.getHorse().getHorseId(),
            schedule.getHorse().getName(), schedule.getCareType(), schedule.getDescription(),
            schedule.getDueDate(), schedule.getIntervalDays(), schedule.getRemindBeforeDays(),
            schedule.getStatus(), schedule.getCompletedDate(),
            performer == null ? null : performer.getUserId(),
            performer == null ? null : performer.getFullName(), schedule.getNotes(),
            schedule.getStatus() == PreventiveCareSchedule.Status.PENDING
                && schedule.getDueDate().isBefore(today));
    }
}
