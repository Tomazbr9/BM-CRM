package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureGroupResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureItemResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.in.GetBudgetStructureUseCase;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetBudgetStructureUseCaseImpl
        implements GetBudgetStructureUseCase {

    private final BudgetAccessService budgetAccessService;
    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetItemRepository budgetItemRepository;

    public GetBudgetStructureUseCaseImpl(
            BudgetAccessService budgetAccessService,
            BudgetGroupRepository budgetGroupRepository,
            BudgetItemRepository budgetItemRepository
    ) {
        this.budgetAccessService = budgetAccessService;
        this.budgetGroupRepository = budgetGroupRepository;
        this.budgetItemRepository = budgetItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetStructureResult execute(
            UUID budgetId
    ) {

        Budget budget =
                budgetAccessService
                        .getBudgetForCurrentUser(
                                budgetId
                        );

        List<BudgetGroup> groups =
                budgetGroupRepository
                        .findByBudgetId(
                                budgetId
                        );

        List<UUID> groupIds =
                groups.stream()
                        .map(BudgetGroup::getId)
                        .toList();

        List<BudgetItem> items =
                budgetItemRepository
                        .findByBudgetGroupIds(
                                groupIds
                        );

        Map<UUID, List<BudgetItem>> itemsByGroup =
                items.stream()
                        .collect(
                                Collectors.groupingBy(
                                        BudgetItem::getBudgetGroupId
                                )
                        );

        List<BudgetStructureGroupResult> groupResults =
                groups.stream()
                        .map(group ->
                                mapGroup(
                                        group,
                                        itemsByGroup
                                )
                        )
                        .toList();

        return new BudgetStructureResult(
                budget.getId(),
                budget.getProjectId(),
                budget.getSinapiTableVersionId(),
                budget.getStatus(),
                budget.getBdiPercentage(),
                groupResults
        );
    }

    private BudgetStructureGroupResult mapGroup(
            BudgetGroup group,
            Map<UUID, List<BudgetItem>> itemsByGroup
    ) {

        List<BudgetStructureItemResult> itemResults =
                itemsByGroup
                        .getOrDefault(
                                group.getId(),
                                List.of()
                        )
                        .stream()
                        .map(this::mapItem)
                        .toList();

        return new BudgetStructureGroupResult(
                group.getId(),
                group.getParentGroupId(),
                group.getName(),
                group.getSortOrder(),
                itemResults
        );
    }

    private BudgetStructureItemResult mapItem(
            BudgetItem item
    ) {

        return new BudgetStructureItemResult(
                item.getId(),
                item.getType(),
                item.getCompositionId(),
                item.getManualItemId(),
                item.getQuantity(),
                item.getUnitPriceSnapshot(),
                item.calculateDirectCost()
        );
    }
}