package com.tomazbr9.buildprice.reports.application.usecase;

import com.tomazbr9.buildprice.reports.application.dto.AbcCurveItemResult;
import com.tomazbr9.buildprice.reports.application.dto.AbcCurveResult;
import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;
import com.tomazbr9.buildprice.reports.application.port.in.GetBudgetAbcCurveUseCase;
import com.tomazbr9.buildprice.reports.application.port.out.BudgetReportGateway;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurve;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurveEntry;
import com.tomazbr9.buildprice.reports.domain.service.AbcCurveCalculator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetBudgetAbcCurveUseCaseImpl
        implements GetBudgetAbcCurveUseCase {

    private final BudgetReportGateway budgetReportGateway;
    private final AbcCurveCalculator abcCurveCalculator;

    public GetBudgetAbcCurveUseCaseImpl(
            BudgetReportGateway budgetReportGateway,
            AbcCurveCalculator abcCurveCalculator
    ) {
        this.budgetReportGateway = budgetReportGateway;
        this.abcCurveCalculator = abcCurveCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public AbcCurveResult execute(UUID budgetId) {

        BudgetReportData budget =
                budgetReportGateway.getByBudgetId(budgetId);

        List<AbcCurveEntry> entries = budget.groups().stream()
                .flatMap(group -> group.items().stream())
                .map(item -> new AbcCurveEntry(
                        item.id(),
                        item.type(),
                        item.code(),
                        item.description(),
                        item.unit(),
                        item.quantity(),
                        item.unitPrice(),
                        item.directCost()
                ))
                .toList();

        AbcCurve curve = abcCurveCalculator.calculate(
                budget.budgetId(),
                entries
        );


        List<AbcCurveItemResult> results = curve.items().stream()
                .map(item -> new AbcCurveItemResult(
                        item.entry().budgetItemId(),
                        item.entry().type(),
                        item.entry().code(),
                        item.entry().description(),
                        item.entry().unit(),
                        item.entry().quantity(),
                        item.entry().unitPrice(),
                        item.entry().directCost(),
                        item.participationPercentage(),
                        item.accumulatedPercentage(),
                        item.classification()
                ))
                .toList();


        return new AbcCurveResult(
                curve.budgetId(),
                curve.totalDirectCost(),
                results
        );
    }
}
