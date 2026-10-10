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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "training_locks", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class TrainingLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id")
    private Integer lockId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_lock_horse"))
    private Horse horse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locked_by", nullable = false,
        foreignKey = @ForeignKey(name = "fk_lock_vet"))
    private User lockedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "injury_id", foreignKey = @ForeignKey(name = "fk_lock_injury"))
    private Injury injury;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_level", length = 20, nullable = false)
    private LockLevel lockLevel;

    @Nationalized
    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "locked_at", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime lockedAt;

    @Column(name = "expected_end_at", columnDefinition = "datetime2")
    private LocalDateTime expectedEndAt;

    @Column(name = "released_at", columnDefinition = "datetime2")
    private LocalDateTime releasedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_by", foreignKey = @ForeignKey(name = "fk_lock_releaser"))
    private User releasedBy;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    public enum LockLevel { FULL, HEAVY_ONLY }
    public enum Status { ACTIVE, RELEASED }
}
