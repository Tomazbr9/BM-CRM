
package com.tomazbr9.buildprice.budget.infrastructure.integration;

import com.tomazbr9.buildprice.budget.api.BudgetReportQuery;
import com.tomazbr9.buildprice.budget.api.BudgetReportView;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.in.GetBudgetStructureUseCase;

import com.tomazbr9.buildprice.projects.api.ProjectBudgetQuery;
import com.tomazbr9.buildprice.projects.api.ProjectBudgetView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BudgetReportQueryImpl implements BudgetReportQuery {

    private final GetBudgetStructureUseCase getBudgetStructureUseCase;
    private final ProjectBudgetQuery projectBudgetQuery;


    @Override
    public BudgetReportView getByBudgetId(UUID budgetId) {

        BudgetStructureResult structure =
                getBudgetStructureUseCase.execute(budgetId);

        ProjectBudgetView project = projectBudgetQuery
                .findById(structure.projectId())
                .orElseThrow(() -> new IllegalStateException(
                        "O projeto vinculado ao orçamento não foi encontrado."
                ));

        List<BudgetReportView.GroupView> groups =
                structure.groups().stream()
                        .map(group -> new BudgetReportView.GroupView(
                                group.id(),
                                group.parentGroupId(),
                                group.name(),
                                group.sortOrder(),
                                group.items().stream()
                                        .map(item -> new BudgetReportView.ItemView(
                                                item.id(),
                                                item.type().name(),
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

        return new BudgetReportView(
                structure.budgetId(),
                project.name(),
                structure.bdiPercentage(),
                structure.directCost(),
                structure.bdiValue(),
                structure.finalPrice(),
                groups
        );
    }
}
