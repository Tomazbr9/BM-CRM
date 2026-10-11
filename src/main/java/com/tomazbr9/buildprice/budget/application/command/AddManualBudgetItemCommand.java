package com.tomazbr9.buildprice.budget.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddManualBudgetItemCommand(
        UUID budgetGroupId,
        String description,
        String unit,
        BigDecimal unitPrice,
        BigDecimal quantity
) {
}