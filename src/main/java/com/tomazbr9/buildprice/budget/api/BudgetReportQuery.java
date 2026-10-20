
package com.tomazbr9.buildprice.budget.api;

import java.util.UUID;

public interface BudgetReportQuery {

    BudgetReportView getByBudgetId(UUID budgetId);
}
