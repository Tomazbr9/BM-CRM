package com.tomazbr9.buildprice.reports.application.port.in;

import java.util.UUID;

public interface ExportBudgetPdfUseCase {

    byte[] execute(UUID budgetId);
}
