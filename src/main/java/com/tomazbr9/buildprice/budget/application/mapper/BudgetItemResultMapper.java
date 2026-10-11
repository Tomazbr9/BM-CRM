package com.tomazbr9.buildprice.budget.application.mapper;

import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;

public final class BudgetItemResultMapper {

    private BudgetItemResultMapper() {
    }

    public static BudgetItemResult toResult(
            BudgetItem item
    ) {
        return new BudgetItemResult(
                item.getId(),
                item.getBudgetGroupId(),
                item.getType(),
                item.getCompositionId(),
                item.getManualItemId(),
                item.getQuantity(),
                item.getUnitPriceSnapshot(),
                item.calculateDirectCost()
        );
    }
}