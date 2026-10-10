package com.horsemanagement.config;

import com.horsemanagement.dto.HealthMetricDto;
import com.horsemanagement.dto.HorseOverviewDto;
import com.horsemanagement.dto.InjuryProgressDto;
import com.horsemanagement.dto.InjurySummaryDto;
import com.horsemanagement.dto.MedicalRecordSummaryDto;
import com.horsemanagement.dto.PreventiveCareDto;
import com.horsemanagement.dto.TreatmentPlanSummaryDto;
import com.horsemanagement.dto.TrainingLockDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** OpenAPI-only concrete views of the existing generic PageResponse DTO. */
public final class VeterinarianPageSchemas {
    private VeterinarianPageSchemas() {
    }

    @Schema(name = "HorseOverviewPage")
    public record HorseOverviewPage(List<HorseOverviewDto> content, int page, int size,
                                    long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "HealthMetricPage")
    public record HealthMetricPage(List<HealthMetricDto> content, int page, int size,
                                   long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "MedicalRecordPage")
    public record MedicalRecordPage(List<MedicalRecordSummaryDto> content, int page, int size,
                                    long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "TreatmentPlanPage")
    public record TreatmentPlanPage(List<TreatmentPlanSummaryDto> content, int page, int size,
                                    long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "InjuryPage")
    public record InjuryPage(List<InjurySummaryDto> content, int page, int size,
                             long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "InjuryProgressPage")
    public record InjuryProgressPage(List<InjuryProgressDto> content, int page, int size,
                                     long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "TrainingLockPage")
    public record TrainingLockPage(List<TrainingLockDto> content, int page, int size,
                                   long totalElements, int totalPages, boolean first, boolean last) {}

    @Schema(name = "PreventiveCarePage")
    public record PreventiveCarePage(List<PreventiveCareDto> content, int page, int size,
                                     long totalElements, int totalPages, boolean first, boolean last) {}
}
