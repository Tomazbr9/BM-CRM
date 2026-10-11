package com.tomazbr9.buildprice.budget.infrastructure.mapper;

import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.infrastructure.entity.BudgetItemJpaEntity;

public final class BudgetItemMapper {

    private BudgetItemMapper() {
    }

    public static BudgetItemJpaEntity toJpaEntity(
            BudgetItem item
    ) {
        return new BudgetItemJpaEntity(
                item.getId(),
                item.getBudgetGroupId(),
                item.getType(),
                item.getCompositionId(),
                item.getManualItemId(),
                item.getQuantity(),
                item.getUnitPriceSnapshot()
        );
    }

    public static BudgetItem toDomain(
            BudgetItemJpaEntity entity
    ) {
        return BudgetItem.restore(
                entity.getId(),
                entity.getBudgetGroupId(),
                entity.getType(),
                entity.getCompositionId(),
                entity.getManualItemId(),
                entity.getQuantity(),
                entity.getUnitPriceSnapshot()
        );
    }
}