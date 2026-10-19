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
class CompleteBudgetUseCaseImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetAccessService budgetAccessService;

    private CompleteBudgetUseCaseImpl useCase;

    private UUID budgetId;
    private Budget budget;

    @BeforeEach
    void setUp() {
        useCase = new CompleteBudgetUseCaseImpl(
                budgetRepository,
                budgetAccessService
        );

        budgetId = UUID.randomUUID();

        budget = Budget.restore(
                budgetId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BudgetStatus.DRAFT,
                new BigDecimal("20.00")
        );
    }

    @Test
    void shouldCompleteDraftBudget() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResult result = useCase.execute(budgetId);

        assertEquals(budgetId, result.id());
        assertEquals(BudgetStatus.COMPLETED, result.status());
        assertEquals(BudgetStatus.COMPLETED, budget.getStatus());

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(result.bdiPercentage())
        );

        verify(budgetRepository).save(budget);
    }

    @Test
    void shouldRejectCompletingAlreadyCompletedBudget() {

        budget.complete();

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        InvalidBudgetStatusTransitionException exception =
                assertThrows(
                        InvalidBudgetStatusTransitionException.class,
                        () -> useCase.execute(budgetId)
                );

        assertEquals(
                "Apenas orçamentos em rascunho podem ser concluídos",
                exception.getMessage()
        );

        assertEquals(BudgetStatus.COMPLETED, budget.getStatus());

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void shouldRejectCompletingInaccessibleBudget() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenThrow(new BudgetNotFoundException());

        assertThrows(
                BudgetNotFoundException.class,
                () -> useCase.execute(budgetId)
        );

        verifyNoInteractions(budgetRepository);
    }
}
