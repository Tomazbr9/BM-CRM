package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.CreateBudgetGroupCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetGroupResult;
import com.tomazbr9.buildprice.budget.application.exception.InvalidParentBudgetGroupException;
import com.tomazbr9.buildprice.budget.application.mapper.BudgetGroupResultMapper;
import com.tomazbr9.buildprice.budget.application.port.in.CreateBudgetGroupUseCase;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateBudgetGroupUseCaseImpl
        implements CreateBudgetGroupUseCase {

    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetAccessService budgetAccessService;

    public CreateBudgetGroupUseCaseImpl(
            BudgetGroupRepository budgetGroupRepository,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetGroupRepository = budgetGroupRepository;
        this.budgetAccessService = budgetAccessService;
    }

    @Override
    @Transactional
    public BudgetGroupResult execute(
            CreateBudgetGroupCommand command
    ) {

        budgetAccessService.getBudgetForCurrentUser(
                command.budgetId()
        );

        if (command.parentGroupId() != null) {

            BudgetGroup parent =
                    budgetGroupRepository
                            .findById(command.parentGroupId())
                            .orElseThrow(
                                    InvalidParentBudgetGroupException::new
                            );

            if (!parent.getBudgetId()
                    .equals(command.budgetId())) {

                throw new InvalidParentBudgetGroupException();
            }
        }

        BudgetGroup group =
                BudgetGroup.create(
                        command.budgetId(),
                        command.parentGroupId(),
                        command.name(),
                        command.sortOrder()
                );

        BudgetGroup saved =
                budgetGroupRepository.save(group);

        return BudgetGroupResultMapper.toResult(saved);
    }
}