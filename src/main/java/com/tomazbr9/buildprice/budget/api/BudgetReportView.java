package com.tomazbr9.buildprice.budget.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BudgetReportView(
        UUID budgetId,
        BigDecimal bdiPercentage,
        BigDecimal directCost,
        BigDecimal bdiValue,
        BigDecimal finalPrice,
        List<GroupView> groups
) {

    public record GroupView(
            UUID id,
            UUID parentGroupId,
            String name,
            Integer sortOrder,
            List<ItemView> items
    ) {
    }

    public record ItemView(
            UUID id,
            String type,
            String code,
            String description,
            String unit,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal directCost
    ) {
    }
}
