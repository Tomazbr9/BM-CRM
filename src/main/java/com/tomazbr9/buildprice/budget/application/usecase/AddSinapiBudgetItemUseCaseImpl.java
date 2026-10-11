package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.AddSinapiBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetGroupNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.CompositionNotAvailableForBudgetException;
import com.tomazbr9.buildprice.budget.application.mapper.BudgetItemResultMapper;
import com.tomazbr9.buildprice.budget.application.port.in.AddSinapiBudgetItemUseCase;
import com.tomazbr9.buildprice.budget.application.port.out.*;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddSinapiBudgetItemUseCaseImpl
        implements AddSinapiBudgetItemUseCase {

    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final ProjectGateway projectGateway;
    private final SinapiCatalogGateway sinapiCatalogGateway;
    private final CurrentUserProvider currentUserProvider;
    private BudgetAccessService budgetAccessService;

    public AddSinapiBudgetItemUseCaseImpl(
            BudgetGroupRepository budgetGroupRepository,
            BudgetRepository budgetRepository,
            BudgetItemRepository budgetItemRepository,
            ProjectGateway projectGateway,
            SinapiCatalogGateway sinapiCatalogGateway,
            CurrentUserProvider currentUserProvider,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetGroupRepository = budgetGroupRepository;
        this.budgetRepository = budgetRepository;
        this.budgetItemRepository = budgetItemRepository;
        this.projectGateway = projectGateway;
        this.sinapiCatalogGateway = sinapiCatalogGateway;
        this.currentUserProvider = currentUserProvider;
        this.budgetAccessService = budgetAccessService;
    }

    @Override
    @Transactional
    public BudgetItemResult execute(
            AddSinapiBudgetItemCommand command
    ) {

        BudgetAccessService.BudgetGroupAccess access =
                budgetAccessService
                        .getBudgetGroupForCurrentUser(
                                command.budgetGroupId()
                        );

        Budget budget =
                access.budget();

        BudgetGroup group =
                access.group();

        SinapiCatalogGateway.CompositionData composition =
                sinapiCatalogGateway
                        .findCompositionById(
                                command.compositionId()
                        )
                        .filter(found ->
                                found.versionId().equals(
                                        budget.getSinapiTableVersionId()
                                )
                        )
                        .orElseThrow(
                                CompositionNotAvailableForBudgetException::new
                        );

        BudgetItem item =
                BudgetItem.createSinapi(
                        group.getId(),
                        composition.id(),
                        command.quantity(),
                        composition.unitCost()
                );

        return BudgetItemResultMapper.toResult(
                budgetItemRepository.save(item)
        );
    }
}