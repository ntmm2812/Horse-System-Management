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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "medical_records", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_medical_horse"))
    private Horse horse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_medical_vet"))
    private User vet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", foreignKey = @ForeignKey(name = "fk_medical_incident"))
    private IncidentReport incident;

    @Column(name = "exam_date", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime examDate;

    @Nationalized
    @Column(name = "reason", length = 255)
    private String reason;

    @Nationalized
    @Column(name = "symptoms", columnDefinition = "nvarchar(max)")
    private String symptoms;

    @Nationalized
    @Column(name = "diagnosis", columnDefinition = "nvarchar(max)")
    private String diagnosis;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status_after", length = 20, nullable = false)
    private Horse.HealthStatus healthStatusAfter;

    @Nationalized
    @Column(name = "notes", columnDefinition = "nvarchar(max)")
    private String notes;
}
