package com.horsemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "horses", uniqueConstraints = {
    @UniqueConstraint(name = "uq_horse_stall", columnNames = "stall_id"),
    @UniqueConstraint(name = "uq_horse_registration", columnNames = "registration_no"),
    @UniqueConstraint(name = "uq_horse_microchip", columnNames = "microchip_no")
})
@Getter
@Setter
@NoArgsConstructor
public class Horse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "horse_id")
    private Integer horseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_horse_owner"))
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true, foreignKey = @ForeignKey(name = "fk_horse_manager"))
    private User manager;

    // Existing FK to stalls.stall_id. Stall entity is outside Phase 1.
    @Column(name = "stall_id")
    private Integer stallId;

    @Nationalized
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Nationalized
    @Column(name = "registration_no", length = 50)
    private String registrationNo;

    @Nationalized
    @Column(name = "microchip_no", length = 50)
    private String microchipNo;

    @Nationalized
    @Column(name = "breed", length = 100)
    private String breed;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20, nullable = false)
    private Gender gender;

    @Nationalized
    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Nationalized
    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "current_weight_kg", precision = 6, scale = 2)
    private BigDecimal currentWeightKg;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", length = 20, nullable = false)
    private HealthStatus healthStatus = HealthStatus.ELIGIBLE;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "readiness_status", length = 20, nullable = false)
    private ReadinessStatus readinessStatus = ReadinessStatus.NOT_READY;

    @Column(name = "is_training_locked", nullable = false)
    private boolean trainingLocked;

    @Nationalized
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime2")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime updatedAt;

    public enum Gender { STALLION, MARE, GELDING, COLT, FILLY }
    public enum HealthStatus { ELIGIBLE, MONITORING, INJURED, QUARANTINE }
    public enum ReadinessStatus { READY, NOT_READY, RESTING }
    public enum Status { ACTIVE, RETIRED, SOLD, DECEASED }
}
