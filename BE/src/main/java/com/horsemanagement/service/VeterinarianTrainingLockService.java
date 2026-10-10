package com.horsemanagement.service;

import com.horsemanagement.dto.CreateTrainingLockRequest;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.ReleaseTrainingLockRequest;
import com.horsemanagement.dto.TrainingEligibilityDto;
import com.horsemanagement.dto.TrainingLockDto;
import com.horsemanagement.entity.TrainingLock;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.HorseNotFoundException;
import com.horsemanagement.exception.InjuryNotFoundException;
import com.horsemanagement.exception.InvalidTrainingLockException;
import com.horsemanagement.exception.TrainingLockConflictException;
import com.horsemanagement.exception.TrainingLockNotFoundException;
import com.horsemanagement.exception.VeterinarianNotFoundException;
import com.horsemanagement.mapper.TrainingLockMapper;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.InjuryRepository;
import com.horsemanagement.repository.TrainingLockRepository;
import com.horsemanagement.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeterinarianTrainingLockService {
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("lockedAt"),
        Sort.Order.desc("lockId"));

    private final TrainingLockRepository locks;
    private final HorseRepository horses;
    private final InjuryRepository injuries;
    private final UserRepository users;
    private final TrainingLockMapper mapper;

    public VeterinarianTrainingLockService(TrainingLockRepository locks,
                                           HorseRepository horses, InjuryRepository injuries,
                                           UserRepository users, TrainingLockMapper mapper) {
        this.locks = locks;
        this.horses = horses;
        this.injuries = injuries;
        this.users = users;
        this.mapper = mapper;
    }

    public PageResponse<TrainingLockDto> list(Integer horseId, TrainingLock.Status status,
                                              TrainingLock.LockLevel lockLevel,
                                              int page, int size) {
        Specification<TrainingLock> filter = (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (horseId != null) {
                predicates.add(builder.equal(root.get("horse").get("horseId"), horseId));
            }
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (lockLevel != null) predicates.add(builder.equal(root.get("lockLevel"), lockLevel));
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return PageResponse.from(locks.findAll(filter, PageRequest.of(page, size, NEWEST_FIRST))
            .map(mapper::toDto));
    }

    public TrainingLockDto get(Integer lockId) {
        return mapper.toDto(requireLock(lockId));
    }

    public PageResponse<TrainingLockDto> horseHistory(Integer horseId, int page, int size) {
        requireHorse(horseId);
        return PageResponse.from(locks.findByHorse_HorseId(horseId,
            PageRequest.of(page, size, NEWEST_FIRST)).map(mapper::toDto));
    }

    public TrainingEligibilityDto eligibility(Integer horseId) {
        var horse = requireHorse(horseId);
        var active = locks.findByHorse_HorseIdAndStatusOrderByLockedAtDescLockIdDesc(horseId,
            TrainingLock.Status.ACTIVE);
        return mapper.toEligibility(horse, active);
    }

    @Transactional
    public TrainingLockDto create(CreateTrainingLockRequest request) {
        // Every writer for this horse must acquire the same SQL Server row lock first.
        var horse = horses.lockForTrainingLocks(request.horseId())
            .orElseThrow(() -> new HorseNotFoundException(request.horseId()));
        var vet = requireVeterinarian(request.lockedBy());
        var lock = new TrainingLock();
        lock.setHorse(horse);
        lock.setLockedBy(vet);
        if (request.injuryId() != null) {
            var injury = injuries.findByInjuryId(request.injuryId())
                .orElseThrow(() -> new InjuryNotFoundException(request.injuryId()));
            if (!injury.getMedicalRecord().getHorse().getHorseId().equals(horse.getHorseId())) {
                throw new InvalidTrainingLockException("injuryId belongs to another horse");
            }
            lock.setInjury(injury);
        }
        lock.setLockLevel(request.lockLevel());
        lock.setReason(request.reason());
        lock.setLockedAt(LocalDateTime.now());
        lock.setExpectedEndAt(request.expectedEndAt());
        lock.setStatus(TrainingLock.Status.ACTIVE);
        try {
            locks.saveAndFlush(lock);
            updateHorseFlag(horse.getHorseId(), true);
        } catch (DataIntegrityViolationException exception) {
            throw new TrainingLockConflictException("Horse, injury or veterinarian changed during creation",
                exception);
        }
        return mapper.toDto(lock);
    }

    @Transactional
    public TrainingLockDto release(Integer lockId, ReleaseTrainingLockRequest request) {
        // Read only the scalar horse ID before acquiring the per-horse write lock.
        var horseId = locks.findHorseIdByLockId(lockId)
            .orElseThrow(() -> new TrainingLockNotFoundException(lockId));
        horses.lockForTrainingLocks(horseId)
            .orElseThrow(() -> new HorseNotFoundException(horseId));
        var lock = requireLock(lockId);
        if (lock.getStatus() != TrainingLock.Status.ACTIVE) {
            throw new TrainingLockConflictException("Training lock is already RELEASED");
        }
        var vet = requireVeterinarian(request.releasedBy());
        lock.setStatus(TrainingLock.Status.RELEASED);
        lock.setReleasedAt(LocalDateTime.now());
        lock.setReleasedBy(vet);
        try {
            locks.saveAndFlush(lock);
            var anotherActive = locks.existsByHorse_HorseIdAndStatus(horseId,
                TrainingLock.Status.ACTIVE);
            updateHorseFlag(horseId, anotherActive);
        } catch (DataIntegrityViolationException exception) {
            throw new TrainingLockConflictException("Horse or veterinarian changed during release",
                exception);
        }
        return mapper.toDto(lock);
    }

    private void updateHorseFlag(Integer horseId, boolean locked) {
        if (horses.updateTrainingLocked(horseId, locked) != 1) {
            throw new TrainingLockConflictException("Could not synchronize horse training lock flag");
        }
    }

    private com.horsemanagement.entity.Horse requireHorse(Integer horseId) {
        return horses.findByHorseId(horseId)
            .orElseThrow(() -> new HorseNotFoundException(horseId));
    }

    private TrainingLock requireLock(Integer lockId) {
        return locks.findByLockId(lockId)
            .orElseThrow(() -> new TrainingLockNotFoundException(lockId));
    }

    private User requireVeterinarian(Integer userId) {
        var vet = users.findById(userId)
            .orElseThrow(() -> new VeterinarianNotFoundException(userId));
        if (!"VETERINARIAN".equals(vet.getRole().getRoleCode())) {
            throw new InvalidTrainingLockException("User must belong to the VETERINARIAN role");
        }
        return vet;
    }
}
