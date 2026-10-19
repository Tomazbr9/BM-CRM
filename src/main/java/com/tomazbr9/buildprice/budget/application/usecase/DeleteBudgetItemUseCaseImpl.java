package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.exception.BudgetItemNotFoundException;
import com.tomazbr9.buildprice.budget.application.port.in.DeleteBudgetItemUseCase;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteBudgetItemUseCaseImpl
        implements DeleteBudgetItemUseCase {

    private final BudgetItemRepository budgetItemRepository;
    private final ManualItemRepository manualItemRepository;
    private final BudgetAccessService budgetAccessService;
    private final BudgetRepository budgetRepository;

    public DeleteBudgetItemUseCaseImpl(
            BudgetItemRepository budgetItemRepository,
            ManualItemRepository manualItemRepository,
            BudgetAccessService budgetAccessService,
            BudgetRepository budgetRepository
    ) {
        this.budgetItemRepository = budgetItemRepository;
        this.manualItemRepository = manualItemRepository;
        this.budgetAccessService = budgetAccessService;
        this.budgetRepository = budgetRepository;
    }

    @Override
    @Transactional
    public void execute(UUID itemId) {

        BudgetItem item = budgetItemRepository
                .findById(itemId)
                .orElseThrow(BudgetItemNotFoundException::new);

        budgetAccessService.getBudgetGroupForCurrentUser(
                item.getBudgetGroupId()
        );

        budgetItemRepository.deleteById(item.getId());

        if (item.getType() == BudgetItemType.MANUAL) {
            manualItemRepository.deleteById(
                    item.getManualItemId()
            );
        }
    }
}