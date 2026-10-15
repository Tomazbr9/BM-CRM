package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.UpdateBudgetItemQuantityCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetItemNotFoundException;
import com.tomazbr9.buildprice.budget.application.mapper.BudgetItemResultMapper;
import com.tomazbr9.buildprice.budget.application.port.in.UpdateBudgetItemQuantityUseCase;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;

import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.infrastructure.mapper.BudgetItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateBudgetItemQuantityUseCaseImpl
        implements UpdateBudgetItemQuantityUseCase {

    private final BudgetItemRepository budgetItemRepository;
    private final BudgetAccessService budgetAccessService;

    public UpdateBudgetItemQuantityUseCaseImpl(
            BudgetItemRepository budgetItemRepository,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetItemRepository = budgetItemRepository;
        this.budgetAccessService = budgetAccessService;
    }

    @Override
    @Transactional
    public BudgetItemResult execute(
            UpdateBudgetItemQuantityCommand command
    ) {

        BudgetItem item =
                budgetItemRepository
                        .findById(command.itemId())
                        .orElseThrow(
                                BudgetItemNotFoundException::new
                        );

        budgetAccessService
                .getBudgetGroupForCurrentUser(
                        item.getBudgetGroupId()
                );

        item.updateQuantity(
                command.quantity()
        );

        BudgetItem saved =
                budgetItemRepository.save(item);

        return BudgetItemResultMapper.toResult(saved);
    }
}