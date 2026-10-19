package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.application.port.in.ReopenBudgetUseCase;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReopenBudgetUseCaseImpl implements ReopenBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final BudgetAccessService budgetAccessService;

    public ReopenBudgetUseCaseImpl(
            BudgetRepository budgetRepository,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetRepository = budgetRepository;
        this.budgetAccessService = budgetAccessService;
    }

    @Override
    @Transactional
    public BudgetResult execute(UUID budgetId) {

        Budget budget = budgetAccessService
                .getBudgetForCurrentUser(budgetId);

        budget.reopen();

        Budget saved = budgetRepository.save(budget);

        return new BudgetResult(
                saved.getId(),
                saved.getProjectId(),
                saved.getSinapiTableVersionId(),
                saved.getStatus(),
                saved.getBdiPercentage()
        );
    }
}