package com.tomazbr9.buildprice.reports.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AbcCurve(
        UUID budgetId,
        BigDecimal totalDirectCost,
        List<AbcCurveItem> items
) {
    public AbcCurve {
        items = List.copyOf(items);
    }
}
