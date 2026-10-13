package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureGroupResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureItemResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.in.GetBudgetStructureUseCase;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.SinapiCatalogGateway;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GetBudgetStructureUseCaseImpl
        implements GetBudgetStructureUseCase {

    private final BudgetAccessService budgetAccessService;
    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final ManualItemRepository manualItemRepository;
    private final SinapiCatalogGateway sinapiCatalogGateway;

    public GetBudgetStructureUseCaseImpl(
            BudgetAccessService budgetAccessService,
            BudgetGroupRepository budgetGroupRepository,
            BudgetItemRepository budgetItemRepository,
            ManualItemRepository manualItemRepository,
            SinapiCatalogGateway sinapiCatalogGateway
    ) {
        this.budgetAccessService = budgetAccessService;
        this.budgetGroupRepository = budgetGroupRepository;
        this.budgetItemRepository = budgetItemRepository;
        this.manualItemRepository = manualItemRepository;
        this.sinapiCatalogGateway = sinapiCatalogGateway;
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

        List<UUID> compositionIds =
                items.stream()
                        .filter(item ->
                                item.getType()
                                        == BudgetItemType.SINAPI
                        )
                        .map(BudgetItem::getCompositionId)
                        .distinct()
                        .toList();

        List<UUID> manualItemIds =
                items.stream()
                        .filter(item ->
                                item.getType()
                                        == BudgetItemType.MANUAL
                        )
                        .map(BudgetItem::getManualItemId)
                        .distinct()
                        .toList();

        Map<UUID, SinapiCatalogGateway.CompositionData>
                compositionsById =
                sinapiCatalogGateway
                        .findCompositionsByIds(
                                compositionIds
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        SinapiCatalogGateway
                                                .CompositionData::id,
                                        Function.identity()
                                )
                        );

        Map<UUID, ManualItem> manualItemsById =
                manualItemRepository
                        .findByIds(
                                manualItemIds
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ManualItem::getId,
                                        Function.identity()
                                )
                        );

        List<BudgetStructureGroupResult> groupResults =
                groups.stream()
                        .map(group ->
                                mapGroup(
                                        group,
                                        itemsByGroup,
                                        compositionsById,
                                        manualItemsById
                                )
                        )
                        .toList();

        BigDecimal directCost =
                groupResults.stream()
                        .map(BudgetStructureGroupResult::directCost)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_EVEN
                        );

        BigDecimal bdiValue =
                calculateBdiValue(
                        directCost,
                        budget.getBdiPercentage()
                );

        BigDecimal finalPrice =
                directCost
                        .add(bdiValue)
                        .setScale(
                                2,
                                RoundingMode.HALF_EVEN
                        );

        return new BudgetStructureResult(
                budget.getId(),
                budget.getProjectId(),
                budget.getSinapiTableVersionId(),
                budget.getStatus(),
                budget.getBdiPercentage(),
                directCost,
                bdiValue,
                finalPrice,
                groupResults
        );
    }

    private BudgetStructureGroupResult mapGroup(
            BudgetGroup group,
            Map<UUID, List<BudgetItem>> itemsByGroup,
            Map<UUID, SinapiCatalogGateway.CompositionData>
                    compositionsById,
            Map<UUID, ManualItem> manualItemsById
    ) {

        List<BudgetStructureItemResult> itemResults =
                itemsByGroup
                        .getOrDefault(
                                group.getId(),
                                List.of()
                        )
                        .stream()
                        .map(item ->
                                mapItem(
                                        item,
                                        compositionsById,
                                        manualItemsById
                                )
                        )
                        .toList();

        BigDecimal directCost =
                itemResults.stream()
                        .map(
                                BudgetStructureItemResult::directCost
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new BudgetStructureGroupResult(
                group.getId(),
                group.getParentGroupId(),
                group.getName(),
                group.getSortOrder(),
                directCost,
                itemResults
        );
    }

    private BudgetStructureItemResult mapItem(
            BudgetItem item,
            Map<UUID, SinapiCatalogGateway.CompositionData>
                    compositionsById,
            Map<UUID, ManualItem> manualItemsById
    ) {

        if (item.getType()
                == BudgetItemType.SINAPI) {

            return mapSinapiItem(
                    item,
                    compositionsById
            );
        }

        return mapManualItem(
                item,
                manualItemsById
        );
    }

    private BudgetStructureItemResult mapSinapiItem(
            BudgetItem item,
            Map<UUID, SinapiCatalogGateway.CompositionData>
                    compositionsById
    ) {

        SinapiCatalogGateway.CompositionData composition =
                compositionsById.get(
                        item.getCompositionId()
                );

        if (composition == null) {
            throw new IllegalStateException(
                    "Composition referenced by budget item was not found"
            );
        }

        return new BudgetStructureItemResult(
                item.getId(),
                item.getType(),
                composition.id(),
                composition.code(),
                composition.description(),
                composition.unit(),
                item.getQuantity(),
                item.getUnitPriceSnapshot(),
                item.calculateDirectCost()
        );
    }

    private BudgetStructureItemResult mapManualItem(
            BudgetItem item,
            Map<UUID, ManualItem> manualItemsById
    ) {

        ManualItem manualItem =
                manualItemsById.get(
                        item.getManualItemId()
                );

        if (manualItem == null) {
            throw new IllegalStateException(
                    "Manual item referenced by budget item was not found"
            );
        }

        return new BudgetStructureItemResult(
                item.getId(),
                item.getType(),
                manualItem.getId(),
                null,
                manualItem.getDescription(),
                manualItem.getUnit(),
                item.getQuantity(),
                item.getUnitPriceSnapshot(),
                item.calculateDirectCost()
        );
    }

    private BigDecimal calculateBdiValue(
            BigDecimal directCost,
            BigDecimal bdiPercentage
    ) {

        return directCost
                .multiply(bdiPercentage)
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_EVEN
                );
    }
}