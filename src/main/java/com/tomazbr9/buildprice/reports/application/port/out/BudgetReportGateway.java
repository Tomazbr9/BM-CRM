
package com.tomazbr9.buildprice.reports.application.port.out;

import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;

import java.util.UUID;

public interface BudgetReportGateway {

    BudgetReportData getByBudgetId(UUID budgetId);
}
