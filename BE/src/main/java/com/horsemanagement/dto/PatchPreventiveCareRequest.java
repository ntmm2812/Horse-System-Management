package com.horsemanagement.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.horsemanagement.entity.PreventiveCareSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/** Omitted fields stay unchanged; explicit null clears only nullable fields. */
@Getter
public class PatchPreventiveCareRequest {
    @JsonIgnore @Schema(hidden = true)
    private final Set<String> supplied = new HashSet<>();
    @JsonIgnore @Schema(hidden = true)
    private final Set<String> unsupported = new HashSet<>();

    @Schema(description = "Required when supplied; null is invalid")
    private PreventiveCareSchedule.CareType careType;
    @Schema(description = "Null clears", maxLength = 255)
    @Size(max = 255) private String description;
    @Schema(description = "Required when supplied; null is invalid")
    private LocalDate dueDate;
    @Schema(description = "Positive when supplied; null clears")
    @Positive private Short intervalDays;
    @Schema(description = "0..255; null is invalid")
    @Min(0) @Max(255) private Short remindBeforeDays;
    @Schema(description = "Null clears", maxLength = 500)
    @Size(max = 500) private String notes;

    public boolean isSupplied(String name) { return supplied.contains(name); }
    public boolean hasChanges() { return !supplied.isEmpty(); }

    @JsonSetter("careType")
    public void setCareType(PreventiveCareSchedule.CareType value) { supplied.add("careType"); careType = value; }
    @JsonSetter("description")
    public void setDescription(String value) { supplied.add("description"); description = value; }
    @JsonSetter("dueDate")
    public void setDueDate(LocalDate value) { supplied.add("dueDate"); dueDate = value; }
    @JsonSetter("intervalDays")
    public void setIntervalDays(Short value) { supplied.add("intervalDays"); intervalDays = value; }
    @JsonSetter("remindBeforeDays")
    public void setRemindBeforeDays(Short value) { supplied.add("remindBeforeDays"); remindBeforeDays = value; }
    @JsonSetter("notes")
    public void setNotes(String value) { supplied.add("notes"); notes = value; }
    @JsonAnySetter
    public void rejectUnsupported(String name, Object value) { unsupported.add(name); }
}
