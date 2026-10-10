package com.horsemanagement.service;

import com.horsemanagement.dto.CompletePreventiveCareRequest;
import com.horsemanagement.dto.CreatePreventiveCareRequest;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.PatchPreventiveCareRequest;
import com.horsemanagement.dto.PreventiveCareDto;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.entity.PreventiveCareSchedule;
import com.horsemanagement.entity.User;
import com.horsemanagement.exception.HorseNotFoundException;
import com.horsemanagement.exception.InvalidPreventiveCareException;
import com.horsemanagement.exception.PreventiveCareConflictException;
import com.horsemanagement.exception.PreventiveCareNotFoundException;
import com.horsemanagement.exception.VeterinarianNotFoundException;
import com.horsemanagement.mapper.PreventiveCareMapper;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.PreventiveCareScheduleRepository;
import com.horsemanagement.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeterinarianPreventiveCareService {
    private static final Sort DUE_FIRST = Sort.by(Sort.Order.asc("dueDate"),
        Sort.Order.asc("scheduleId"));
    private final PreventiveCareScheduleRepository schedules;
    private final HorseRepository horses;
    private final UserRepository users;
    private final PreventiveCareMapper mapper;

    public VeterinarianPreventiveCareService(PreventiveCareScheduleRepository schedules,
        HorseRepository horses, UserRepository users, PreventiveCareMapper mapper) {
        this.schedules = schedules;
        this.horses = horses;
        this.users = users;
        this.mapper = mapper;
    }

    public PageResponse<PreventiveCareDto> list(Integer horseId,
        PreventiveCareSchedule.CareType careType, PreventiveCareSchedule.Status status,
        LocalDate fromDueDate, LocalDate toDueDate, boolean pendingPastDueOnly,
        int page, int size) {
        validatePage(page, size);
        if (horseId != null && horseId <= 0) {
            throw new InvalidPreventiveCareException("horseId must be positive");
        }
        if (fromDueDate != null && toDueDate != null && fromDueDate.isAfter(toDueDate)) {
            throw new InvalidPreventiveCareException("fromDueDate must not be after toDueDate");
        }
        if (pendingPastDueOnly && status != null && status != PreventiveCareSchedule.Status.PENDING) {
            throw new InvalidPreventiveCareException("pendingPastDueOnly requires status PENDING or no status filter");
        }
        var today = LocalDate.now();
        Specification<PreventiveCareSchedule> filter = (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (horseId != null) predicates.add(builder.equal(root.get("horse").get("horseId"), horseId));
            if (careType != null) predicates.add(builder.equal(root.get("careType"), careType));
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (fromDueDate != null) predicates.add(builder.greaterThanOrEqualTo(root.get("dueDate"), fromDueDate));
            if (toDueDate != null) predicates.add(builder.lessThanOrEqualTo(root.get("dueDate"), toDueDate));
            if (pendingPastDueOnly) {
                predicates.add(builder.equal(root.get("status"), PreventiveCareSchedule.Status.PENDING));
                predicates.add(builder.lessThan(root.get("dueDate"), today));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(schedules.findAll(filter, PageRequest.of(page, size, DUE_FIRST))
            .map(schedule -> mapper.toDto(schedule, today)));
    }

    public PreventiveCareDto get(Integer scheduleId) {
        return mapper.toDto(requireSchedule(scheduleId), LocalDate.now());
    }

    public PageResponse<PreventiveCareDto> horseHistory(Integer horseId,
        PreventiveCareSchedule.Status status, int page, int size) {
        validatePage(page, size);
        requireHorse(horseId);
        return list(horseId, null, status, null, null, false, page, size);
    }

    @Transactional
    public PreventiveCareDto create(CreatePreventiveCareRequest request) {
        var schedule = new PreventiveCareSchedule();
        schedule.setHorse(requireHorse(request.horseId()));
        schedule.setCareType(request.careType());
        schedule.setDescription(request.description());
        schedule.setDueDate(request.dueDate());
        schedule.setIntervalDays(request.intervalDays());
        schedule.setRemindBeforeDays(request.remindBeforeDays() == null ? (short) 3 : request.remindBeforeDays());
        schedule.setNotes(request.notes());
        schedule.setStatus(PreventiveCareSchedule.Status.PENDING);
        return save(schedule);
    }

    @Transactional
    public PreventiveCareDto patch(Integer scheduleId, PatchPreventiveCareRequest request) {
        if (!request.getUnsupported().isEmpty()) {
            throw new InvalidPreventiveCareException("Unsupported fields: " + request.getUnsupported());
        }
        if (!request.hasChanges()) {
            throw new InvalidPreventiveCareException("At least one editable field is required");
        }
        var schedule = requireLockedSchedule(scheduleId);
        if (request.isSupplied("careType")) {
            if (request.getCareType() == null) throw new InvalidPreventiveCareException("careType cannot be null");
            schedule.setCareType(request.getCareType());
        }
        if (request.isSupplied("description")) schedule.setDescription(request.getDescription());
        if (request.isSupplied("dueDate")) {
            if (request.getDueDate() == null) throw new InvalidPreventiveCareException("dueDate cannot be null");
            schedule.setDueDate(request.getDueDate());
        }
        if (request.isSupplied("intervalDays")) schedule.setIntervalDays(request.getIntervalDays());
        if (request.isSupplied("remindBeforeDays")) {
            if (request.getRemindBeforeDays() == null) {
                throw new InvalidPreventiveCareException("remindBeforeDays cannot be null");
            }
            schedule.setRemindBeforeDays(request.getRemindBeforeDays());
        }
        if (request.isSupplied("notes")) schedule.setNotes(request.getNotes());
        return save(schedule);
    }

    @Transactional
    public PreventiveCareDto complete(Integer scheduleId, CompletePreventiveCareRequest request) {
        var schedule = requireLockedSchedule(scheduleId);
        requireOpen(schedule);
        var completedDate = request.completedDate() == null ? LocalDate.now() : request.completedDate();
        if (completedDate.isAfter(LocalDate.now())) {
            throw new InvalidPreventiveCareException("completedDate cannot be in the future");
        }
        var veterinarian = requireVeterinarian(request.performedBy());
        schedule.setStatus(PreventiveCareSchedule.Status.DONE);
        schedule.setCompletedDate(completedDate);
        schedule.setPerformedBy(veterinarian);
        if (request.notes() != null) schedule.setNotes(request.notes());
        return save(schedule);
    }

    @Transactional
    public PreventiveCareDto cancel(Integer scheduleId) {
        var schedule = requireLockedSchedule(scheduleId);
        requireOpen(schedule);
        schedule.setStatus(PreventiveCareSchedule.Status.CANCELLED);
        return save(schedule);
    }

    private PreventiveCareDto save(PreventiveCareSchedule schedule) {
        try {
            schedules.saveAndFlush(schedule);
        } catch (DataIntegrityViolationException exception) {
            throw new PreventiveCareConflictException("Preventive care reference or constraint changed", exception);
        }
        return mapper.toDto(schedule, LocalDate.now());
    }

    private Horse requireHorse(Integer horseId) {
        return horses.findByHorseId(horseId).orElseThrow(() -> new HorseNotFoundException(horseId));
    }

    private PreventiveCareSchedule requireSchedule(Integer scheduleId) {
        return schedules.findByScheduleId(scheduleId)
            .orElseThrow(() -> new PreventiveCareNotFoundException(scheduleId));
    }

    private PreventiveCareSchedule requireLockedSchedule(Integer scheduleId) {
        return schedules.lockById(scheduleId)
            .orElseThrow(() -> new PreventiveCareNotFoundException(scheduleId));
    }

    private User requireVeterinarian(Integer userId) {
        var user = users.findById(userId)
            .orElseThrow(() -> new VeterinarianNotFoundException(userId));
        if (!"VETERINARIAN".equals(user.getRole().getRoleCode())) {
            throw new InvalidPreventiveCareException("performedBy must belong to the VETERINARIAN role");
        }
        return user;
    }

    private void requireOpen(PreventiveCareSchedule schedule) {
        if (schedule.getStatus() != PreventiveCareSchedule.Status.PENDING
            && schedule.getStatus() != PreventiveCareSchedule.Status.OVERDUE) {
            throw new PreventiveCareConflictException("Only PENDING or OVERDUE schedules can change status");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidPreventiveCareException("page must be >= 0 and size must be 1..100");
        }
    }
}
