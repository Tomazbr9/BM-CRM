package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import com.tomazbr9.buildprice.budget.domain.exception.InvalidBudgetStatusTransitionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReopenBudgetUseCaseImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetAccessService budgetAccessService;

    private ReopenBudgetUseCaseImpl useCase;

    private UUID budgetId;
    private Budget budget;

    @BeforeEach
    void setUp() {
        useCase = new ReopenBudgetUseCaseImpl(
                budgetRepository,
                budgetAccessService
        );

        budgetId = UUID.randomUUID();

        budget = Budget.restore(
                budgetId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BudgetStatus.COMPLETED,
                new BigDecimal("20.00")
        );
    }

    @Test
    void shouldReopenCompletedBudget() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResult result = useCase.execute(budgetId);

        assertEquals(budgetId, result.id());
        assertEquals(BudgetStatus.DRAFT, result.status());
        assertEquals(BudgetStatus.DRAFT, budget.getStatus());

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(result.bdiPercentage())
        );

        verify(budgetRepository).save(budget);
    }

    @Test
    void shouldRejectReopeningDraftBudget() {

        Budget draftBudget = Budget.restore(
                budgetId,
                budget.getProjectId(),
                budget.getSinapiTableVersionId(),
                BudgetStatus.DRAFT,
                budget.getBdiPercentage()
        );

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(draftBudget);

        InvalidBudgetStatusTransitionException exception =
                assertThrows(
                        InvalidBudgetStatusTransitionException.class,
                        () -> useCase.execute(budgetId)
                );

        assertEquals(
                "Apenas orçamentos concluídos podem ser reabertos",
                exception.getMessage()
        );

        assertEquals(BudgetStatus.DRAFT, draftBudget.getStatus());

        verify(budgetRepository, never()).save(any());
    }

}
