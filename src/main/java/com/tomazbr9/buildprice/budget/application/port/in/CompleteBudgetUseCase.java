package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import java.util.UUID;

public interface CompleteBudgetUseCase {

    BudgetResult execute(UUID budgetId);
}