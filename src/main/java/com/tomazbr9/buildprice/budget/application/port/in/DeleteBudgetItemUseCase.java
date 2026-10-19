package com.tomazbr9.buildprice.budget.application.port.in;

import java.util.UUID;

public interface DeleteBudgetItemUseCase {

    void execute(UUID itemId);

}