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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "injury_progress_logs", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class InjuryProgressLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "injury_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_progress_injury"))
    private Injury injury;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by", foreignKey = @ForeignKey(name = "fk_progress_user"))
    private User loggedBy;

    @Column(name = "log_date", nullable = false, columnDefinition = "datetime2")
    private LocalDateTime logDate;

    @Column(name = "recovery_percent", columnDefinition = "tinyint")
    private Short recoveryPercent;

    @Column(name = "pain_level", columnDefinition = "tinyint")
    private Short painLevel;

    @Nationalized
    @Column(name = "notes", columnDefinition = "nvarchar(max)")
    private String notes;

    @Nationalized
    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
