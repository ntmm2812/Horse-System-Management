package com.horsemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Nationalized;

@Entity
@Immutable
@Table(name = "supply_categories", schema = "dbo", uniqueConstraints =
    @UniqueConstraint(name = "uq_supply_category_name", columnNames = "category_name"))
@Getter
@Setter
@NoArgsConstructor
public class SupplyCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Nationalized
    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 20, nullable = false)
    private CategoryType categoryType;

    @Nationalized
    @Column(name = "description", length = 255)
    private String description;

    public enum CategoryType { FEED, MEDICINE, EQUIPMENT }
}
