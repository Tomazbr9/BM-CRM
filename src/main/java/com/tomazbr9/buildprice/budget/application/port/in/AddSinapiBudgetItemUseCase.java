package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.command.AddSinapiBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;

public interface AddSinapiBudgetItemUseCase {

    BudgetItemResult execute(
            AddSinapiBudgetItemCommand command
    );
}