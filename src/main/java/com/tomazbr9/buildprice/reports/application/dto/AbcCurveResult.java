
package com.tomazbr9.buildprice.reports.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AbcCurveResult(
        UUID budgetId,
        BigDecimal totalDirectCost,
        List<AbcCurveItemResult> items
) {
}
