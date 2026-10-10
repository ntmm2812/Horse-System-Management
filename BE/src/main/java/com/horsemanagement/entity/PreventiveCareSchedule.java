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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "preventive_care_schedules", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class PreventiveCareSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_care_horse"))
    private Horse horse;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "care_type", length = 20, nullable = false)
    private CareType careType;

    @Nationalized
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "interval_days")
    private Short intervalDays;

    @Column(name = "remind_before_days", nullable = false, columnDefinition = "tinyint")
    private Short remindBeforeDays = 3;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", foreignKey = @ForeignKey(name = "fk_care_user"))
    private User performedBy;

    @Nationalized
    @Column(name = "notes", length = 500)
    private String notes;

    public enum CareType { VACCINATION, DEWORMING, FARRIER, DENTAL, OTHER }
    public enum Status { PENDING, DONE, OVERDUE, CANCELLED }
}
