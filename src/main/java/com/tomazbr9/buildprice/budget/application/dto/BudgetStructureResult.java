package com.tomazbr9.buildprice.budget.application.dto;

import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BudgetStructureResult(
        UUID budgetId,
        UUID projectId,
        UUID sinapiTableVersionId,
        BudgetStatus status,
        BigDecimal bdiPercentage,
        BigDecimal directCost,
        BigDecimal bdiValue,
        BigDecimal finalPrice,
        List<BudgetStructureGroupResult> groups
) {
}