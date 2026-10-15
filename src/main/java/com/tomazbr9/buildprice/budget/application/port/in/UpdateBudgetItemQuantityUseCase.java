package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.command.UpdateBudgetItemQuantityCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;

public interface UpdateBudgetItemQuantityUseCase {

    BudgetItemResult execute(
            UpdateBudgetItemQuantityCommand command
    );
}