package com.tomazbr9.buildprice.reports.application.port.out;

import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;

public interface BudgetPdfExporter {

    byte[] export(BudgetReportData budget);
}
