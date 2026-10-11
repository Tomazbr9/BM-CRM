package com.tomazbr9.buildprice.budget.application.port.in;

import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;

import java.util.UUID;

public interface GetBudgetStructureUseCase {

    BudgetStructureResult execute(
            UUID budgetId
    );
}