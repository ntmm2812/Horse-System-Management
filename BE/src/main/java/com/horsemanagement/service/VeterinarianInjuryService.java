package com.horsemanagement.service;

import com.horsemanagement.dto.CreateInjuryProgressRequest;
import com.horsemanagement.dto.CreateInjuryRequest;
import com.horsemanagement.dto.InjuryDetailDto;
import com.horsemanagement.dto.InjuryProgressDto;
import com.horsemanagement.dto.InjurySummaryDto;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.PatchInjuryRequest;
import com.horsemanagement.entity.Injury;
import com.horsemanagement.entity.InjuryProgressLog;
import com.horsemanagement.exception.InjuryConflictException;
import com.horsemanagement.exception.InjuryNotFoundException;
import com.horsemanagement.exception.InvalidInjuryException;
import com.horsemanagement.exception.MedicalRecordNotFoundException;
import com.horsemanagement.exception.VeterinarianNotFoundException;
import com.horsemanagement.mapper.InjuryMapper;
import com.horsemanagement.repository.InjuryProgressLogRepository;
import com.horsemanagement.repository.InjuryRepository;
import com.horsemanagement.repository.MedicalRecordRepository;
import com.horsemanagement.repository.UserRepository;
import java.time.LocalDate;
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
public class VeterinarianInjuryService {
    private final InjuryRepository injuries;
    private final InjuryProgressLogRepository progressLogs;
    private final MedicalRecordRepository records;
    private final UserRepository users;
    private final InjuryMapper mapper;

    public VeterinarianInjuryService(InjuryRepository injuries,
                                     InjuryProgressLogRepository progressLogs,
                                     MedicalRecordRepository records,
                                     UserRepository users, InjuryMapper mapper) {
        this.injuries = injuries;
        this.progressLogs = progressLogs;
        this.records = records;
        this.users = users;
        this.mapper = mapper;
    }

    public PageResponse<InjurySummaryDto> list(Integer horseId, Injury.Status status,
                                               Injury.Severity severity, Injury.BodySystem bodySystem,
                                               int page, int size) {
        Specification<Injury> filter = (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (horseId != null) {
                predicates.add(builder.equal(root.get("medicalRecord").get("horse").get("horseId"),
                    horseId));
            }
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (severity != null) predicates.add(builder.equal(root.get("severity"), severity));
            if (bodySystem != null) predicates.add(builder.equal(root.get("bodySystem"), bodySystem));
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("injuryId")));
        return PageResponse.from(injuries.findAll(filter, pageable).map(mapper::toSummary));
    }

    public InjuryDetailDto get(Integer injuryId) {
        return mapper.toDetail(requireInjury(injuryId));
    }

    @Transactional
    public InjuryDetailDto create(CreateInjuryRequest request) {
        var record = records.findByRecordId(request.recordId())
            .orElseThrow(() -> new MedicalRecordNotFoundException(request.recordId()));
        var injury = new Injury();
        injury.setMedicalRecord(record);
        injury.setBodyPart(request.bodyPart());
        injury.setBodySystem(request.bodySystem());
        injury.setModelMeshId(request.modelMeshId());
        injury.setPositionX(request.positionX());
        injury.setPositionY(request.positionY());
        injury.setPositionZ(request.positionZ());
        injury.setInjuryType(request.injuryType());
        injury.setSeverity(request.severity());
        injury.setStatus(Injury.Status.ACTIVE);
        injury.setOccurredDate(request.occurredDate());
        injury.setDescription(request.description());
        try {
            injuries.saveAndFlush(injury);
        } catch (DataIntegrityViolationException exception) {
            throw new InjuryConflictException("Medical record changed while creating injury", exception);
        }
        return mapper.toDetail(injury);
    }

    @Transactional
    public InjuryDetailDto patch(Integer injuryId, PatchInjuryRequest request) {
        var injury = requireInjury(injuryId);
        if (!request.hasChanges()) {
            throw new InvalidInjuryException("At least one field must be provided");
        }
        if (request.isSupplied("bodyPart") &&
            (request.getBodyPart() == null || request.getBodyPart().isBlank())) {
            throw new InvalidInjuryException("bodyPart must not be blank");
        }
        if (request.isSupplied("bodySystem") && request.getBodySystem() == null) {
            throw new InvalidInjuryException("bodySystem must not be null");
        }
        if (request.isSupplied("severity") && request.getSeverity() == null) {
            throw new InvalidInjuryException("severity must not be null");
        }
        if (request.isSupplied("status") && request.getStatus() == null) {
            throw new InvalidInjuryException("status must not be null");
        }

        var nextStatus = request.isSupplied("status") ? request.getStatus() : injury.getStatus();
        var nextOccurredDate = request.isSupplied("occurredDate")
            ? request.getOccurredDate() : injury.getOccurredDate();
        var nextHealedDate = request.isSupplied("healedDate")
            ? request.getHealedDate() : injury.getHealedDate();
        if (nextStatus != Injury.Status.HEALED && nextHealedDate != null
            && injury.getStatus() == Injury.Status.HEALED && request.isSupplied("status")) {
            throw new InjuryConflictException(
                "Supply healedDate: null when reopening a healed injury");
        }
        validateDates(nextStatus, nextOccurredDate, nextHealedDate);

        if (request.isSupplied("bodyPart")) injury.setBodyPart(request.getBodyPart());
        if (request.isSupplied("bodySystem")) injury.setBodySystem(request.getBodySystem());
        if (request.isSupplied("modelMeshId")) injury.setModelMeshId(request.getModelMeshId());
        if (request.isSupplied("positionX")) injury.setPositionX(request.getPositionX());
        if (request.isSupplied("positionY")) injury.setPositionY(request.getPositionY());
        if (request.isSupplied("positionZ")) injury.setPositionZ(request.getPositionZ());
        if (request.isSupplied("injuryType")) injury.setInjuryType(request.getInjuryType());
        if (request.isSupplied("severity")) injury.setSeverity(request.getSeverity());
        if (request.isSupplied("status")) injury.setStatus(request.getStatus());
        if (request.isSupplied("occurredDate")) injury.setOccurredDate(request.getOccurredDate());
        if (request.isSupplied("healedDate")) injury.setHealedDate(request.getHealedDate());
        if (request.isSupplied("description")) injury.setDescription(request.getDescription());
        try {
            injuries.saveAndFlush(injury);
        } catch (DataIntegrityViolationException exception) {
            throw new InjuryConflictException("Injury changed while updating", exception);
        }
        return mapper.toDetail(injury);
    }

    public PageResponse<InjuryProgressDto> progress(Integer injuryId, int page, int size) {
        requireInjury(injuryId);
        var pageable = PageRequest.of(page, size,
            Sort.by(Sort.Order.desc("logDate"), Sort.Order.desc("progressId")));
        return PageResponse.from(progressLogs.findByInjury_InjuryId(injuryId, pageable)
            .map(mapper::toProgress));
    }

    @Transactional
    public InjuryProgressDto addProgress(Integer injuryId, CreateInjuryProgressRequest request) {
        var injury = requireInjury(injuryId);
        var user = users.findById(request.loggedBy())
            .orElseThrow(() -> new VeterinarianNotFoundException(request.loggedBy()));
        if (!"VETERINARIAN".equals(user.getRole().getRoleCode())) {
            throw new InvalidInjuryException("loggedBy must belong to the VETERINARIAN role");
        }
        var log = new InjuryProgressLog();
        log.setInjury(injury);
        log.setLoggedBy(user);
        log.setLogDate(request.logDate() == null ? LocalDateTime.now() : request.logDate());
        log.setRecoveryPercent(request.recoveryPercent());
        log.setPainLevel(request.painLevel());
        log.setNotes(request.notes());
        log.setImageUrl(request.imageUrl());
        try {
            progressLogs.saveAndFlush(log);
        } catch (DataIntegrityViolationException exception) {
            throw new InjuryConflictException("Injury or veterinarian changed while logging progress",
                exception);
        }
        return mapper.toProgress(log);
    }

    private Injury requireInjury(Integer injuryId) {
        return injuries.findByInjuryId(injuryId)
            .orElseThrow(() -> new InjuryNotFoundException(injuryId));
    }

    private static void validateDates(Injury.Status status, LocalDate occurredDate,
                                      LocalDate healedDate) {
        if (status == Injury.Status.HEALED && healedDate == null) {
            throw new InvalidInjuryException("healedDate is required when status is HEALED");
        }
        if (healedDate != null && occurredDate != null && healedDate.isBefore(occurredDate)) {
            throw new InvalidInjuryException("healedDate must be on or after occurredDate");
        }
        if (status != Injury.Status.HEALED && healedDate != null) {
            throw new InvalidInjuryException("healedDate requires status HEALED");
        }
    }
}
