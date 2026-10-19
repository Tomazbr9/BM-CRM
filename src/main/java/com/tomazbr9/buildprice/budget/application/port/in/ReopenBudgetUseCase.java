package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import java.util.UUID;

public interface ReopenBudgetUseCase {

    BudgetResult execute(UUID budgetId);
}