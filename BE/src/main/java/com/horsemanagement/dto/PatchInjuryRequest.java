package com.horsemanagement.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.horsemanagement.entity.Injury;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/** A supplied JSON null clears nullable fields; an omitted field remains unchanged. */
@Getter
public class PatchInjuryRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private final Set<String> supplied = new HashSet<>();

    @Schema(description = "Required when supplied; null is invalid", maxLength = 100)
    @Size(max = 100)
    private String bodyPart;
    @Schema(description = "MUSCLE, BONE, TENDON, LIGAMENT, HOOF, SKIN, OTHER; null is invalid")
    private Injury.BodySystem bodySystem;
    @Schema(description = "Mesh ID; null clears", maxLength = 100)
    @Size(max = 100)
    private String modelMeshId;
    @Schema(description = "DECIMAL(9,4); null clears")
    @Digits(integer = 5, fraction = 4)
    private BigDecimal positionX;
    @Schema(description = "DECIMAL(9,4); null clears")
    @Digits(integer = 5, fraction = 4)
    private BigDecimal positionY;
    @Schema(description = "DECIMAL(9,4); null clears")
    @Digits(integer = 5, fraction = 4)
    private BigDecimal positionZ;
    @Schema(description = "Injury type; null clears", maxLength = 100)
    @Size(max = 100)
    private String injuryType;
    @Schema(description = "MINOR, MODERATE, SEVERE; null is invalid")
    private Injury.Severity severity;
    @Schema(description = "ACTIVE, RECOVERING, HEALED; null is invalid")
    private Injury.Status status;
    @Schema(description = "Occurrence date; null clears")
    private LocalDate occurredDate;
    @Schema(description = "Required for HEALED; null clears when reopening")
    private LocalDate healedDate;
    @Schema(description = "Description; null clears")
    private String description;

    public boolean isSupplied(String field) { return supplied.contains(field); }
    public boolean hasChanges() { return !supplied.isEmpty(); }

    @JsonSetter("bodyPart")
    public void setBodyPart(String value) { supplied.add("bodyPart"); bodyPart = value; }
    @JsonSetter("bodySystem")
    public void setBodySystem(Injury.BodySystem value) { supplied.add("bodySystem"); bodySystem = value; }
    @JsonSetter("modelMeshId")
    public void setModelMeshId(String value) { supplied.add("modelMeshId"); modelMeshId = value; }
    @JsonSetter("positionX")
    public void setPositionX(BigDecimal value) { supplied.add("positionX"); positionX = value; }
    @JsonSetter("positionY")
    public void setPositionY(BigDecimal value) { supplied.add("positionY"); positionY = value; }
    @JsonSetter("positionZ")
    public void setPositionZ(BigDecimal value) { supplied.add("positionZ"); positionZ = value; }
    @JsonSetter("injuryType")
    public void setInjuryType(String value) { supplied.add("injuryType"); injuryType = value; }
    @JsonSetter("severity")
    public void setSeverity(Injury.Severity value) { supplied.add("severity"); severity = value; }
    @JsonSetter("status")
    public void setStatus(Injury.Status value) { supplied.add("status"); status = value; }
    @JsonSetter("occurredDate")
    public void setOccurredDate(LocalDate value) { supplied.add("occurredDate"); occurredDate = value; }
    @JsonSetter("healedDate")
    public void setHealedDate(LocalDate value) { supplied.add("healedDate"); healedDate = value; }
    @JsonSetter("description")
    public void setDescription(String value) { supplied.add("description"); description = value; }
}
