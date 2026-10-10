package com.horsemanagement.mapper;

import com.horsemanagement.dto.PrescriptionItemDto;
import com.horsemanagement.dto.TreatmentPlanDetailDto;
import com.horsemanagement.dto.TreatmentPlanSummaryDto;
import com.horsemanagement.entity.PrescriptionItem;
import com.horsemanagement.entity.TreatmentPlan;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TreatmentPlanMapper {
    public TreatmentPlanSummaryDto toSummary(TreatmentPlan plan) {
        return new TreatmentPlanSummaryDto(plan.getTreatmentId(),
            plan.getMedicalRecord().getRecordId(),
            plan.getMedicalRecord().getHorse().getHorseId(),
            plan.getMedicalRecord().getHorse().getName(),
            plan.getDescription(), plan.getStartDate(), plan.getEndDate(),
            plan.getStatus(), plan.getCreatedAt());
    }

    public TreatmentPlanDetailDto toDetail(TreatmentPlan plan, List<PrescriptionItem> items) {
        return new TreatmentPlanDetailDto(plan.getTreatmentId(),
            plan.getMedicalRecord().getRecordId(),
            plan.getMedicalRecord().getHorse().getHorseId(),
            plan.getMedicalRecord().getHorse().getName(),
            plan.getDescription(), plan.getStartDate(), plan.getEndDate(),
            plan.getStatus(), plan.getCreatedAt(),
            items.stream().map(this::toPrescription).toList());
    }

    public PrescriptionItemDto toPrescription(PrescriptionItem item) {
        return new PrescriptionItemDto(item.getPrescriptionItemId(),
            item.getTreatmentPlan().getTreatmentId(), item.getSupply().getSupplyId(),
            item.getSupply().getSupplyName(), item.getDosage(), item.getFrequency(),
            item.getDurationDays(), item.getRoute(), item.getInstructions());
    }
}
