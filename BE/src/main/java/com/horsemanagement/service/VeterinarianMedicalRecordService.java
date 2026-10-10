package com.horsemanagement.service;

import com.horsemanagement.dto.CreateMedicalRecordRequest;
import com.horsemanagement.dto.MedicalRecordDetailDto;
import com.horsemanagement.dto.MedicalRecordSummaryDto;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.entity.MedicalRecord;
import com.horsemanagement.exception.HorseNotFoundException;
import com.horsemanagement.exception.IncidentAlreadyLinkedException;
import com.horsemanagement.exception.InvalidMedicalRecordException;
import com.horsemanagement.exception.MedicalRecordNotFoundException;
import com.horsemanagement.exception.VeterinarianNotFoundException;
import com.horsemanagement.mapper.MedicalRecordMapper;
import com.horsemanagement.repository.HorseRepository;
import com.horsemanagement.repository.IncidentReportRepository;
import com.horsemanagement.repository.MedicalRecordRepository;
import com.horsemanagement.repository.UserRepository;
import java.sql.SQLException;
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
public class VeterinarianMedicalRecordService {
    private final MedicalRecordRepository records;
    private final HorseRepository horses;
    private final UserRepository users;
    private final IncidentReportRepository incidents;
    private final MedicalRecordMapper mapper;

    public VeterinarianMedicalRecordService(MedicalRecordRepository records,
                                            HorseRepository horses,
                                            UserRepository users,
                                            IncidentReportRepository incidents,
                                            MedicalRecordMapper mapper) {
        this.records = records;
        this.horses = horses;
        this.users = users;
        this.incidents = incidents;
        this.mapper = mapper;
    }

    public PageResponse<MedicalRecordSummaryDto> list(Integer horseId, Integer vetId,
                                                       LocalDate fromDate, LocalDate toDate,
                                                       int page, int size) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new InvalidMedicalRecordException("fromDate must not be after toDate");
        }
        Specification<MedicalRecord> filter = (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (horseId != null) {
                predicates.add(builder.equal(root.get("horse").get("horseId"), horseId));
            }
            if (vetId != null) {
                predicates.add(builder.equal(root.get("vet").get("userId"), vetId));
            }
            if (fromDate != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("examDate"),
                    fromDate.atStartOfDay()));
            }
            if (toDate != null && !toDate.equals(LocalDate.MAX)) {
                predicates.add(builder.lessThan(root.get("examDate"),
                    toDate.plusDays(1).atStartOfDay()));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var pageable = PageRequest.of(page, size,
            Sort.by(Sort.Order.desc("examDate"), Sort.Order.desc("recordId")));
        return PageResponse.from(records.findAll(filter, pageable).map(mapper::toSummary));
    }

    public MedicalRecordDetailDto get(Integer recordId) {
        return mapper.toDetail(records.findByRecordId(recordId)
            .orElseThrow(() -> new MedicalRecordNotFoundException(recordId)));
    }

    @Transactional
    public MedicalRecordDetailDto create(CreateMedicalRecordRequest request) {
        var horse = horses.findById(request.horseId())
            .orElseThrow(() -> new HorseNotFoundException(request.horseId()));
        var vet = users.findById(request.vetId())
            .orElseThrow(() -> new VeterinarianNotFoundException(request.vetId()));
        if (!"VETERINARIAN".equals(vet.getRole().getRoleCode())) {
            throw new InvalidMedicalRecordException("vetId must belong to the VETERINARIAN role");
        }

        var record = new MedicalRecord();
        record.setHorse(horse);
        record.setVet(vet);
        if (request.incidentId() != null) {
            var incident = incidents.findById(request.incidentId())
                .orElseThrow(() -> new InvalidMedicalRecordException("incidentId does not exist"));
            if (!incident.getHorse().getHorseId().equals(horse.getHorseId())) {
                throw new InvalidMedicalRecordException("incidentId belongs to another horse");
            }
            if (records.existsByIncident_IncidentId(request.incidentId())) {
                throw new IncidentAlreadyLinkedException(request.incidentId());
            }
            record.setIncident(incident);
        }
        record.setExamDate(request.examDate() == null ? LocalDateTime.now() : request.examDate());
        record.setReason(request.reason());
        record.setSymptoms(request.symptoms());
        record.setDiagnosis(request.diagnosis());
        record.setHealthStatusAfter(request.healthStatusAfter());
        record.setNotes(request.notes());

        try {
            records.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            if (request.incidentId() != null && isUniqueViolation(exception)) {
                throw new IncidentAlreadyLinkedException(request.incidentId());
            }
            throw exception;
        }
        // JPQL updates this one column only; training lock and readiness are untouched.
        if (horses.updateHealthStatus(horse.getHorseId(), request.healthStatusAfter()) != 1) {
            throw new HorseNotFoundException(horse.getHorseId());
        }
        return mapper.toDetail(record);
    }

    private static boolean isUniqueViolation(Throwable exception) {
        for (var cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && (sql.getErrorCode() == 2601
                || sql.getErrorCode() == 2627)) {
                return true;
            }
        }
        return false;
    }
}
