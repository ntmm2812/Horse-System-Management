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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "prescription_items", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_item_id")
    private Integer prescriptionItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_presc_treatment"))
    private TreatmentPlan treatmentPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supply_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_presc_supply"))
    private Supply supply;

    @Nationalized
    @Column(name = "dosage", length = 100, nullable = false)
    private String dosage;

    @Nationalized
    @Column(name = "frequency", length = 100, nullable = false)
    private String frequency;

    @Column(name = "duration_days")
    private Short durationDays;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "route", length = 20)
    private Route route;

    @Nationalized
    @Column(name = "instructions", length = 255)
    private String instructions;

    public enum Route { ORAL, INJECTION, TOPICAL, IV, OTHER }
}
