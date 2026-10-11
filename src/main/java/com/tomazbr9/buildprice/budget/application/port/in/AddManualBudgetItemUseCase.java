package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.command.AddManualBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;

public interface AddManualBudgetItemUseCase {

    BudgetItemResult execute(
            AddManualBudgetItemCommand command
    );
}