package com.tomazbr9.buildprice.budget.application.dto;

import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetItemResult(
        UUID id,
        UUID budgetGroupId,
        BudgetItemType type,
        UUID compositionId,
        UUID manualItemId,
        BigDecimal quantity,
        BigDecimal unitPriceSnapshot,
        BigDecimal directCost
) {
}