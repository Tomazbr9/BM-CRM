package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.AddManualBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetGroupNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.mapper.BudgetItemResultMapper;
import com.tomazbr9.buildprice.budget.application.port.in.AddManualBudgetItemUseCase;
import com.tomazbr9.buildprice.budget.application.port.out.*;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddManualBudgetItemUseCaseImpl
        implements AddManualBudgetItemUseCase {

    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetRepository budgetRepository;
    private final ManualItemRepository manualItemRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final ProjectGateway projectGateway;
    private final CurrentUserProvider currentUserProvider;
    private final BudgetAccessService budgetAccessService;

    public AddManualBudgetItemUseCaseImpl(
            BudgetGroupRepository budgetGroupRepository,
            BudgetRepository budgetRepository,
            ManualItemRepository manualItemRepository,
            BudgetItemRepository budgetItemRepository,
            ProjectGateway projectGateway,
            CurrentUserProvider currentUserProvider,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetGroupRepository = budgetGroupRepository;
        this.budgetRepository = budgetRepository;
        this.manualItemRepository = manualItemRepository;
        this.budgetItemRepository = budgetItemRepository;
        this.projectGateway = projectGateway;
        this.currentUserProvider = currentUserProvider;
        this.budgetAccessService = budgetAccessService;
    }

    @Override
    @Transactional
    public BudgetItemResult execute(
            AddManualBudgetItemCommand command
    ) {

        BudgetAccessService.BudgetGroupAccess access =
                budgetAccessService
                        .getBudgetGroupForCurrentUser(
                                command.budgetGroupId()
                        );

        access.budget().ensureEditable();

        Budget budget =
                access.budget();

        BudgetGroup group =
                access.group();


        ManualItem manualItem =
                ManualItem.create(
                        budget.getId(),
                        command.description(),
                        command.unit(),
                        command.unitPrice()
                );

        ManualItem savedManualItem =
                manualItemRepository.save(manualItem);

        BudgetItem budgetItem =
                BudgetItem.createManual(
                        group.getId(),
                        savedManualItem.getId(),
                        command.quantity(),
                        savedManualItem.getUnitPrice()
                );

        return BudgetItemResultMapper.toResult(
                budgetItemRepository.save(budgetItem)
        );
    }
}