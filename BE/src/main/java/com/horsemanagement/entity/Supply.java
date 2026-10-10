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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Nationalized;

@Entity
@Immutable
@Table(name = "supplies", schema = "dbo", uniqueConstraints =
    @UniqueConstraint(name = "uq_supply_code", columnNames = "supply_code"))
@Getter
@Setter
@NoArgsConstructor
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supply_id")
    private Integer supplyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_supply_category"))
    private SupplyCategory category;

    @Nationalized
    @Column(name = "supply_code", length = 30, nullable = false)
    private String supplyCode;

    @Nationalized
    @Column(name = "supply_name", length = 150, nullable = false)
    private String supplyName;

    @Nationalized
    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "min_stock_level", precision = 12, scale = 2, nullable = false)
    private BigDecimal minStockLevel;

    @Nationalized
    @Column(name = "description", length = 255)
    private String description;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    public enum Status { ACTIVE, INACTIVE }
}
