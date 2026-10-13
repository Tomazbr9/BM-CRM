package com.tomazbr9.buildprice.budget.application.dto;

import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetStructureItemResult(
        UUID id,
        BudgetItemType type,
        UUID referenceId,
        String code,
        String description,
        String unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal directCost
) {
}