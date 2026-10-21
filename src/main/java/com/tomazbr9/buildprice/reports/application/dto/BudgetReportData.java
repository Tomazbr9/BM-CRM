
package com.tomazbr9.buildprice.reports.application.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public record BudgetReportData(
        UUID budgetId,
        String projectName,
        String stateAbbreviation,
        YearMonth referenceMonth,
        String taxReliefRegime,
        BigDecimal bdiPercentage,
        BigDecimal directCost,
        BigDecimal bdiValue,
        BigDecimal finalPrice,
        List<GroupData> groups
) {

    public record GroupData(
            UUID id,
            UUID parentGroupId,
            String name,
            Integer sortOrder,
            List<ItemData> items
    ) {
    }

    public record ItemData(
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
