package com.horsemanagement.mapper;

import com.horsemanagement.dto.InjuryDetailDto;
import com.horsemanagement.dto.InjuryProgressDto;
import com.horsemanagement.dto.InjurySummaryDto;
import com.horsemanagement.entity.Injury;
import com.horsemanagement.entity.InjuryProgressLog;
import org.springframework.stereotype.Component;

@Component
public class InjuryMapper {
    public InjurySummaryDto toSummary(Injury injury) {
        var horse = injury.getMedicalRecord().getHorse();
        return new InjurySummaryDto(injury.getInjuryId(), injury.getMedicalRecord().getRecordId(),
            horse.getHorseId(), horse.getName(), injury.getBodyPart(), injury.getBodySystem(),
            injury.getSeverity(), injury.getStatus(), injury.getOccurredDate(), injury.getHealedDate());
    }

    public InjuryDetailDto toDetail(Injury injury) {
        var horse = injury.getMedicalRecord().getHorse();
        return new InjuryDetailDto(injury.getInjuryId(), injury.getMedicalRecord().getRecordId(),
            horse.getHorseId(), horse.getName(), injury.getBodyPart(), injury.getBodySystem(),
            injury.getModelMeshId(), injury.getPositionX(), injury.getPositionY(),
            injury.getPositionZ(), injury.getInjuryType(), injury.getSeverity(), injury.getStatus(),
            injury.getOccurredDate(), injury.getHealedDate(), injury.getDescription());
    }

    public InjuryProgressDto toProgress(InjuryProgressLog log) {
        var user = log.getLoggedBy();
        return new InjuryProgressDto(log.getProgressId(), log.getInjury().getInjuryId(),
            user == null ? null : user.getUserId(), user == null ? null : user.getFullName(),
            log.getLogDate(), log.getRecoveryPercent(), log.getPainLevel(),
            log.getNotes(), log.getImageUrl());
    }
}
