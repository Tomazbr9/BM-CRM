
package com.tomazbr9.buildprice.reports.application.dto;

import com.tomazbr9.buildprice.reports.domain.enums.AbcClassification;

import java.math.BigDecimal;
import java.util.UUID;

public record AbcCurveItemResult(
        UUID budgetItemId,
        String type,
        String code,
        String description,
        String unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal directCost,
        BigDecimal participationPercentage,
        BigDecimal accumulatedPercentage,
        AbcClassification classification
) {
}
