package com.tomazbr9.buildprice.reports.application.usecase;

import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;
import com.tomazbr9.buildprice.reports.application.port.in.ExportBudgetPdfUseCase;
import com.tomazbr9.buildprice.reports.application.port.out.BudgetPdfExporter;
import com.tomazbr9.buildprice.reports.application.port.out.BudgetReportGateway;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExportBudgetPdfUseCaseImpl
        implements ExportBudgetPdfUseCase {

    private final BudgetReportGateway budgetReportGateway;
    private final BudgetPdfExporter budgetPdfExporter;

    public ExportBudgetPdfUseCaseImpl(
            BudgetReportGateway budgetReportGateway,
            BudgetPdfExporter budgetPdfExporter
    ) {
        this.budgetReportGateway = budgetReportGateway;
        this.budgetPdfExporter = budgetPdfExporter;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] execute(UUID budgetId) {

        BudgetReportData budget =
                budgetReportGateway.getByBudgetId(budgetId);

        return budgetPdfExporter.export(budget);
    }
}
