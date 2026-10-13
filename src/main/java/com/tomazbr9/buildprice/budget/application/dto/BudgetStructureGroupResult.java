package com.tomazbr9.buildprice.budget.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BudgetStructureGroupResult(
        UUID id,
        UUID parentGroupId,
        String name,
        Integer sortOrder,
        BigDecimal directCost,
        List<BudgetStructureItemResult> items
) {
}