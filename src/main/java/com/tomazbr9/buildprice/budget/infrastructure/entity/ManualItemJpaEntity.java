package com.tomazbr9.buildprice.budget.infrastructure.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_manual_items")
public class ManualItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "budget_id", nullable = false)
    private UUID budgetId;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 15,
            scale = 4
    )
    private BigDecimal unitPrice;

    protected ManualItemJpaEntity() {
    }

    public ManualItemJpaEntity(
            UUID id,
            UUID budgetId,
            String description,
            String unit,
            BigDecimal unitPrice
    ) {
        this.id = id;
        this.budgetId = budgetId;
        this.description = description;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBudgetId() {
        return budgetId;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}