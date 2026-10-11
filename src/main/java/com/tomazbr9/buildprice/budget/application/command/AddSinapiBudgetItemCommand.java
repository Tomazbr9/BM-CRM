package com.tomazbr9.buildprice.budget.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddSinapiBudgetItemCommand(
        UUID budgetGroupId,
        UUID compositionId,
        BigDecimal quantity
) {
}