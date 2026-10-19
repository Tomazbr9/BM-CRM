package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.command.UpdateBudgetBdiCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;

public interface UpdateBudgetBdiUseCase {

    BudgetResult execute(UpdateBudgetBdiCommand command);
}