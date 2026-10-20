
package com.tomazbr9.buildprice.reports.domain.model;

import com.tomazbr9.buildprice.reports.domain.enums.AbcClassification;

import java.math.BigDecimal;

public record AbcCurveItem(
        AbcCurveEntry entry,
        BigDecimal participationPercentage,
        BigDecimal accumulatedPercentage,
        AbcClassification classification
) {
}
