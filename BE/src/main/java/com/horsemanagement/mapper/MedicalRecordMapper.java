package com.horsemanagement.mapper;

import com.horsemanagement.dto.MedicalRecordDetailDto;
import com.horsemanagement.dto.MedicalRecordSummaryDto;
import com.horsemanagement.entity.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {
    public MedicalRecordSummaryDto toSummary(MedicalRecord record) {
        return new MedicalRecordSummaryDto(record.getRecordId(),
            record.getHorse().getHorseId(), record.getHorse().getName(),
            record.getVet().getUserId(), record.getVet().getFullName(),
            record.getExamDate(), record.getReason(), record.getHealthStatusAfter());
    }

    public MedicalRecordDetailDto toDetail(MedicalRecord record) {
        return new MedicalRecordDetailDto(record.getRecordId(),
            record.getHorse().getHorseId(), record.getHorse().getName(),
            record.getVet().getUserId(), record.getVet().getFullName(),
            record.getIncident() == null ? null : record.getIncident().getIncidentId(),
            record.getExamDate(), record.getReason(), record.getSymptoms(),
            record.getDiagnosis(), record.getHealthStatusAfter(), record.getNotes());
    }
}
