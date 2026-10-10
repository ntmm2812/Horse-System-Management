package com.horsemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/** Only the fields needed to validate an existing incident; no incident CRUD. */
@Entity
@Immutable
@Table(name = "incident_reports", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
public class IncidentReport {
    @Id
    @Column(name = "incident_id")
    private Integer incidentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_incident_horse"))
    private Horse horse;
}
