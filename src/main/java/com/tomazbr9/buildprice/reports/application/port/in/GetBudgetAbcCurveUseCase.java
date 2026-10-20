
package com.tomazbr9.buildprice.reports.application.port.in;

import com.tomazbr9.buildprice.reports.application.dto.AbcCurveResult;

import java.util.UUID;

public interface GetBudgetAbcCurveUseCase {

    AbcCurveResult execute(UUID budgetId);
}
