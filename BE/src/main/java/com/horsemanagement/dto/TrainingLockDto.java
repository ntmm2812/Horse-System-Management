package com.horsemanagement.dto;

import com.horsemanagement.entity.TrainingLock;
import java.time.LocalDateTime;

public record TrainingLockDto(
    Integer lockId, Integer horseId, String horseName,
    Integer injuryId, String injuryBodyPart,
    Integer lockedBy, String lockedByName,
    TrainingLock.LockLevel lockLevel, String reason,
    LocalDateTime lockedAt, LocalDateTime expectedEndAt,
    LocalDateTime releasedAt, Integer releasedBy, String releasedByName,
    TrainingLock.Status status
) {}
