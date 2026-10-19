package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.UpdateBudgetBdiCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.domain.exception.BudgetNotEditableException;
import com.tomazbr9.buildprice.budget.application.port.in.UpdateBudgetBdiUseCase;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateBudgetBdiUseCaseImpl
        implements UpdateBudgetBdiUseCase {

    private final BudgetRepository budgetRepository;
    private final BudgetAccessService budgetAccessService;

    public UpdateBudgetBdiUseCaseImpl(
            BudgetRepository budgetRepository,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetRepository = budgetRepository;
        this.budgetAccessService = budgetAccessService;
    }

    @Override
    @Transactional
    public BudgetResult execute(UpdateBudgetBdiCommand command) {

        Budget budget = budgetAccessService
                .getBudgetForCurrentUser(command.budgetId());

        budget.ensureEditable();

        budget.updateBdiPercentage(command.bdiPercentage());

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