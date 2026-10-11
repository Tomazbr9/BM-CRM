package com.tomazbr9.buildprice.budget.application.service;

import com.tomazbr9.buildprice.budget.application.exception.BudgetGroupNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ProjectGateway;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BudgetAccessService {

    private final BudgetRepository budgetRepository;
    private final BudgetGroupRepository budgetGroupRepository;
    private final ProjectGateway projectGateway;
    private final CurrentUserProvider currentUserProvider;
    private final BudgetAccessService budgetAccessService;

    public BudgetAccessService(
            BudgetRepository budgetRepository,
            BudgetGroupRepository budgetGroupRepository,
            ProjectGateway projectGateway,
            CurrentUserProvider currentUserProvider,
            BudgetAccessService budgetAccessService
    ) {
        this.budgetRepository = budgetRepository;
        this.budgetGroupRepository = budgetGroupRepository;
        this.projectGateway = projectGateway;
        this.currentUserProvider = currentUserProvider;
        this.budgetAccessService = budgetAccessService;
    }

    public Budget getBudgetForCurrentUser(
            UUID budgetId
    ) {

        UUID userId =
                currentUserProvider.getCurrentUserId();

        Budget budget =
                budgetRepository
                        .findById(budgetId)
                        .orElseThrow(
                                BudgetNotFoundException::new
                        );

        boolean belongsToUser =
                projectGateway
                        .findById(budget.getProjectId())
                        .filter(project ->
                                project.userId().equals(userId)
                        )
                        .isPresent();

        if (!belongsToUser) {
            throw new BudgetNotFoundException();
        }

        return budget;
    }

    public BudgetGroupAccess getBudgetGroupForCurrentUser(
            UUID budgetGroupId
    ) {

        BudgetGroup group =
                budgetGroupRepository
                        .findById(budgetGroupId)
                        .orElseThrow(
                                BudgetGroupNotFoundException::new
                        );

        Budget budget =
                getBudgetForCurrentUser(
                        group.getBudgetId()
                );

        return new BudgetGroupAccess(
                budget,
                group
        );
    }

    public record BudgetGroupAccess(
            Budget budget,
            BudgetGroup group
    ) {
    }
}