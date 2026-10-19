package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.UpdateBudgetBdiCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.domain.exception.BudgetNotEditableException;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
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
class UpdateBudgetBdiUseCaseImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetAccessService budgetAccessService;

    private UpdateBudgetBdiUseCaseImpl useCase;

    private UUID budgetId;
    private Budget budget;

    @BeforeEach
    void setUp() {
        useCase = new UpdateBudgetBdiUseCaseImpl(
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
    void shouldUpdateBudgetBdiPercentage() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBudgetBdiCommand command =
                new UpdateBudgetBdiCommand(
                        budgetId,
                        new BigDecimal("25.00")
                );

        BudgetResult result = useCase.execute(command);

        assertEquals(budgetId, result.id());
        assertEquals(BudgetStatus.DRAFT, result.status());

        assertEquals(
                0,
                new BigDecimal("25.00")
                        .compareTo(result.bdiPercentage())
        );

        verify(budgetRepository).save(budget);
    }

    @Test
    void shouldAllowZeroBdiPercentage() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResult result = useCase.execute(
                new UpdateBudgetBdiCommand(
                        budgetId,
                        BigDecimal.ZERO
                )
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.bdiPercentage())
        );

        verify(budgetRepository).save(budget);
    }

    @Test
    void shouldRejectNegativeBdiPercentage() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        UpdateBudgetBdiCommand command =
                new UpdateBudgetBdiCommand(
                        budgetId,
                        new BigDecimal("-5.00")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(command)
        );

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenBudgetIsCompleted() {

        budget.complete();

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenReturn(budget);

        UpdateBudgetBdiCommand command =
                new UpdateBudgetBdiCommand(
                        budgetId,
                        new BigDecimal("30.00")
                );

        assertThrows(
                BudgetNotEditableException.class,
                () -> useCase.execute(command)
        );

        verify(budgetRepository, never()).save(any());

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(budget.getBdiPercentage())
        );
    }

    @Test
    void shouldRejectUpdateWhenBudgetIsNotAccessible() {

        when(budgetAccessService.getBudgetForCurrentUser(budgetId))
                .thenThrow(new BudgetNotFoundException());

        UpdateBudgetBdiCommand command =
                new UpdateBudgetBdiCommand(
                        budgetId,
                        new BigDecimal("25.00")
                );

        assertThrows(
                BudgetNotFoundException.class,
                () -> useCase.execute(command)
        );

        verifyNoInteractions(budgetRepository);
    }
}
