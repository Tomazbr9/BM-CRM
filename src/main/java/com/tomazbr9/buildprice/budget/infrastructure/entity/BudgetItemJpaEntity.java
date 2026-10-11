package com.tomazbr9.buildprice.budget.infrastructure.entity;

import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_budget_items")
public class BudgetItemJpaEntity {

    @Id
    private UUID id;

    @Column(
            name = "budget_group_id",
            nullable = false
    )
    private UUID budgetGroupId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 20
    )
    private BudgetItemType type;

    @Column(name = "composition_id")
    private UUID compositionId;

    @Column(name = "manual_item_id")
    private UUID manualItemId;

    @Column(
            nullable = false,
            precision = 15,
            scale = 4
    )
    private BigDecimal quantity;

    @Column(
            name = "unit_price_snapshot",
            nullable = false,
            precision = 15,
            scale = 4
    )
    private BigDecimal unitPriceSnapshot;

    protected BudgetItemJpaEntity() {
    }

    public BudgetItemJpaEntity(
            UUID id,
            UUID budgetGroupId,
            BudgetItemType type,
            UUID compositionId,
            UUID manualItemId,
            BigDecimal quantity,
            BigDecimal unitPriceSnapshot
    ) {
        this.id = id;
        this.budgetGroupId = budgetGroupId;
        this.type = type;
        this.compositionId = compositionId;
        this.manualItemId = manualItemId;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBudgetGroupId() {
        return budgetGroupId;
    }

    public BudgetItemType getType() {
        return type;
    }

    public UUID getCompositionId() {
        return compositionId;
    }

    public UUID getManualItemId() {
        return manualItemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }
}