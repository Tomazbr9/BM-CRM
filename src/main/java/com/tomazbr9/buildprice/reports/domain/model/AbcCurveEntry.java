
package com.tomazbr9.buildprice.reports.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AbcCurveEntry(
        UUID budgetItemId,
        String type,
        String code,
        String description,
        String unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal directCost
) {
}
