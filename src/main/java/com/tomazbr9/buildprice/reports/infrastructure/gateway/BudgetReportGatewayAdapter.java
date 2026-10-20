
package com.tomazbr9.buildprice.reports.infrastructure.gateway;

import com.tomazbr9.buildprice.budget.api.BudgetReportQuery;
import com.tomazbr9.buildprice.budget.api.BudgetReportView;
import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;
import com.tomazbr9.buildprice.reports.application.port.out.BudgetReportGateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BudgetReportGatewayAdapter implements BudgetReportGateway {

    private final BudgetReportQuery budgetReportQuery;

    @Override
    public BudgetReportData getByBudgetId(UUID budgetId) {

        BudgetReportView view =
                budgetReportQuery.getByBudgetId(budgetId);

        List<BudgetReportData.GroupData> groups =
                view.groups().stream()
                        .map(group -> new BudgetReportData.GroupData(
                                group.id(),
                                group.parentGroupId(),
                                group.name(),
                                group.sortOrder(),
                                group.items().stream()
                                        .map(item -> new BudgetReportData.ItemData(
                                                item.id(),
                                                item.type(),
                                                item.code(),
                                                item.description(),
                                                item.unit(),
                                                item.quantity(),
                                                item.unitPrice(),
                                                item.directCost()
                                        ))
                                        .toList()
                        ))
                        .toList();

        return new BudgetReportData(
                view.budgetId(),
                view.bdiPercentage(),
                view.directCost(),
                view.bdiValue(),
                view.finalPrice(),
                groups
        );
    }
}
