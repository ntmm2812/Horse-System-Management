package com.horsemanagement.service;

import com.horsemanagement.dto.CreatePrescriptionItemRequest;
import com.horsemanagement.dto.CreateTreatmentPlanRequest;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.PatchTreatmentPlanRequest;
import com.horsemanagement.dto.PrescriptionItemDto;
import com.horsemanagement.dto.TreatmentPlanDetailDto;
import com.horsemanagement.dto.TreatmentPlanSummaryDto;
import com.horsemanagement.entity.PrescriptionItem;
import com.horsemanagement.entity.SupplyCategory;
import com.horsemanagement.entity.TreatmentPlan;
import com.horsemanagement.exception.InvalidTreatmentPlanException;
import com.horsemanagement.exception.MedicalRecordNotFoundException;
import com.horsemanagement.exception.SupplyNotFoundException;
import com.horsemanagement.exception.TreatmentConflictException;
import com.horsemanagement.exception.TreatmentPlanNotFoundException;
import com.horsemanagement.mapper.TreatmentPlanMapper;
import com.horsemanagement.repository.MedicalRecordRepository;
import com.horsemanagement.repository.PrescriptionItemRepository;
import com.horsemanagement.repository.SupplyRepository;
import com.horsemanagement.repository.TreatmentPlanRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeterinarianTreatmentPlanService {
    private final TreatmentPlanRepository plans;
    private final PrescriptionItemRepository prescriptions;
    private final MedicalRecordRepository records;
    private final SupplyRepository supplies;
    private final TreatmentPlanMapper mapper;

    public VeterinarianTreatmentPlanService(TreatmentPlanRepository plans,
                                            PrescriptionItemRepository prescriptions,
                                            MedicalRecordRepository records,
                                            SupplyRepository supplies,
                                            TreatmentPlanMapper mapper) {
        this.plans = plans;
        this.prescriptions = prescriptions;
        this.records = records;
        this.supplies = supplies;
        this.mapper = mapper;
    }

    public PageResponse<TreatmentPlanSummaryDto> list(Integer recordId,
                                                       TreatmentPlan.Status status,
                                                       int page, int size) {
        Specification<TreatmentPlan> filter = (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (recordId != null) {
                predicates.add(builder.equal(root.get("medicalRecord").get("recordId"), recordId));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var pageable = PageRequest.of(page, size,
            Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("treatmentId")));
        return PageResponse.from(plans.findAll(filter, pageable).map(mapper::toSummary));
    }

    public TreatmentPlanDetailDto get(Integer treatmentId) {
        var plan = requirePlan(treatmentId);
        return mapper.toDetail(plan, prescriptions
            .findByTreatmentPlan_TreatmentIdOrderByPrescriptionItemIdAsc(treatmentId));
    }

    @Transactional
    public TreatmentPlanDetailDto create(CreateTreatmentPlanRequest request) {
        validateDates(request.startDate(), request.endDate());
        var record = records.findById(request.recordId())
            .orElseThrow(() -> new MedicalRecordNotFoundException(request.recordId()));
        var plan = new TreatmentPlan();
        plan.setMedicalRecord(record);
        plan.setDescription(request.description());
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setStatus(TreatmentPlan.Status.ACTIVE);
        try {
            plans.saveAndFlush(plan);
        } catch (DataIntegrityViolationException exception) {
            throw new TreatmentConflictException("Medical record changed while creating treatment", exception);
        }
        return mapper.toDetail(plan, List.of());
    }

    @Transactional
    public TreatmentPlanDetailDto patch(Integer treatmentId, PatchTreatmentPlanRequest request) {
        var plan = requirePlan(treatmentId);
        if (request.description() == null && request.endDate() == null && request.status() == null) {
            throw new InvalidTreatmentPlanException("At least one field must be provided");
        }
        if (request.description() != null) {
            if (request.description().isBlank()) {
                throw new InvalidTreatmentPlanException("description must not be blank");
            }
            plan.setDescription(request.description());
        }
        if (request.endDate() != null) {
            validateDates(plan.getStartDate(), request.endDate());
            plan.setEndDate(request.endDate());
        }
        if (request.status() != null) {
            plan.setStatus(request.status());
        }
        try {
            plans.saveAndFlush(plan);
        } catch (DataIntegrityViolationException exception) {
            throw new TreatmentConflictException("Treatment changed while updating", exception);
        }
        return mapper.toDetail(plan, prescriptions
            .findByTreatmentPlan_TreatmentIdOrderByPrescriptionItemIdAsc(treatmentId));
    }

    public List<PrescriptionItemDto> listPrescriptions(Integer treatmentId) {
        requirePlan(treatmentId);
        return prescriptions.findByTreatmentPlan_TreatmentIdOrderByPrescriptionItemIdAsc(treatmentId)
            .stream().map(mapper::toPrescription).toList();
    }

    @Transactional
    public PrescriptionItemDto addPrescription(Integer treatmentId,
                                               CreatePrescriptionItemRequest request) {
        var plan = requirePlan(treatmentId);
        var supply = supplies.findBySupplyId(request.supplyId())
            .orElseThrow(() -> new SupplyNotFoundException(request.supplyId()));
        if (supply.getCategory().getCategoryType() != SupplyCategory.CategoryType.MEDICINE) {
            throw new InvalidTreatmentPlanException("supplyId must belong to a MEDICINE category");
        }
        var item = new PrescriptionItem();
        item.setTreatmentPlan(plan);
        item.setSupply(supply);
        item.setDosage(request.dosage());
        item.setFrequency(request.frequency());
        item.setDurationDays(request.durationDays());
        item.setRoute(request.route());
        item.setInstructions(request.instructions());
        try {
            prescriptions.saveAndFlush(item);
        } catch (DataIntegrityViolationException exception) {
            throw new TreatmentConflictException("Treatment or supply changed while adding prescription",
                exception);
        }
        return mapper.toPrescription(item);
    }

    private TreatmentPlan requirePlan(Integer treatmentId) {
        return plans.findByTreatmentId(treatmentId)
            .orElseThrow(() -> new TreatmentPlanNotFoundException(treatmentId));
    }

    private static void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidTreatmentPlanException("endDate must be on or after startDate");
        }
    }
}
