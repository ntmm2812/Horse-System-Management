package com.horsemanagement.mapper;

import com.horsemanagement.dto.TrainingEligibilityDto;
import com.horsemanagement.dto.TrainingLockDto;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.TrainingLock;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TrainingLockMapper {
    public TrainingLockDto toDto(TrainingLock lock) {
        var injury = lock.getInjury();
        var releaser = lock.getReleasedBy();
        return new TrainingLockDto(lock.getLockId(), lock.getHorse().getHorseId(),
            lock.getHorse().getName(), injury == null ? null : injury.getInjuryId(),
            injury == null ? null : injury.getBodyPart(), lock.getLockedBy().getUserId(),
            lock.getLockedBy().getFullName(), lock.getLockLevel(), lock.getReason(),
            lock.getLockedAt(), lock.getExpectedEndAt(), lock.getReleasedAt(),
            releaser == null ? null : releaser.getUserId(),
            releaser == null ? null : releaser.getFullName(), lock.getStatus());
    }

    public TrainingEligibilityDto toEligibility(Horse horse, List<TrainingLock> activeLocks) {
        boolean full = activeLocks.stream()
            .anyMatch(lock -> lock.getLockLevel() == TrainingLock.LockLevel.FULL);
        boolean heavy = full || activeLocks.stream()
            .anyMatch(lock -> lock.getLockLevel() == TrainingLock.LockLevel.HEAVY_ONLY);
        return new TrainingEligibilityDto(horse.getHorseId(), horse.getName(),
            horse.getHealthStatus(), horse.getReadinessStatus(), horse.isTrainingLocked(),
            activeLocks.stream().map(this::toDto).toList(),
            new TrainingEligibilityDto.LockRestrictions(full, full, heavy));
    }
}
