package com.horsemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "injuries", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class Injury {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "injury_id")
    private Integer injuryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_injury_record"))
    private MedicalRecord medicalRecord;

    @Nationalized
    @Column(name = "body_part", length = 100, nullable = false)
    private String bodyPart;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "body_system", length = 20, nullable = false)
    private BodySystem bodySystem;

    @Nationalized
    @Column(name = "model_mesh_id", length = 100)
    private String modelMeshId;

    @Column(name = "position_x", precision = 9, scale = 4)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 9, scale = 4)
    private BigDecimal positionY;

    @Column(name = "position_z", precision = 9, scale = 4)
    private BigDecimal positionZ;

    @Nationalized
    @Column(name = "injury_type", length = 100)
    private String injuryType;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    private Severity severity;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "occurred_date")
    private LocalDate occurredDate;

    @Column(name = "healed_date")
    private LocalDate healedDate;

    @Nationalized
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    public enum BodySystem { MUSCLE, BONE, TENDON, LIGAMENT, HOOF, SKIN, OTHER }
    public enum Severity { MINOR, MODERATE, SEVERE }
    public enum Status { ACTIVE, RECOVERING, HEALED }
}
