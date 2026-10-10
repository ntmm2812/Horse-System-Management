package com.horsemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "horse_health_metrics", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class HorseHealthMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_metric_horse"))
    private Horse horse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", foreignKey = @ForeignKey(name = "fk_metric_user"))
    private User recordedBy;

    @Column(name = "recorded_at", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime recordedAt;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "resting_heart_rate")
    private Short restingHeartRate;

    @Column(name = "body_temperature", precision = 4, scale = 1)
    private BigDecimal bodyTemperature;

    @Column(name = "respiratory_rate")
    private Short respiratoryRate;

    @Nationalized
    @Column(name = "notes", length = 500)
    private String notes;
}
