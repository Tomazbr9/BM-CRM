package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.exception.BudgetItemNotFoundException;

import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteBudgetItemUseCaseImplTest {

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private ManualItemRepository manualItemRepository;

    @Mock
    private BudgetAccessService budgetAccessService;

    @Mock
    private BudgetRepository budgetRepository;

    private DeleteBudgetItemUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBudgetItemUseCaseImpl(
                budgetItemRepository,
                manualItemRepository,
                budgetAccessService,
                budgetRepository
        );
    }

    @Test
    void shouldDeleteSinapiBudgetItem() {

        UUID itemId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        BudgetItem item = BudgetItem.createSinapi(
                groupId,
                compositionId,
                new BigDecimal("2.50"),
                new BigDecimal("100.00")
        );

        when(budgetItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        useCase.execute(itemId);

        verify(budgetAccessService)
                .getBudgetGroupForCurrentUser(groupId);

        verify(budgetItemRepository)
                .deleteById(item.getId());

        verifyNoInteractions(manualItemRepository);
    }

    @Test
    void shouldDeleteManualBudgetItemAndManualRecord() {

        UUID itemId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID manualItemId = UUID.randomUUID();

        BudgetItem item = BudgetItem.createManual(
                groupId,
                manualItemId,
                new BigDecimal("3.00"),
                new BigDecimal("150.00")
        );

        when(budgetItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        useCase.execute(itemId);

        verify(budgetAccessService)
                .getBudgetGroupForCurrentUser(groupId);

        var inOrder = inOrder(
                budgetItemRepository,
                manualItemRepository
        );

        inOrder.verify(budgetItemRepository)
                .deleteById(item.getId());

        inOrder.verify(manualItemRepository)
                .deleteById(manualItemId);
    }

    @Test
    void shouldThrowWhenBudgetItemDoesNotExist() {

        UUID itemId = UUID.randomUUID();

        when(budgetItemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetItemNotFoundException.class,
                () -> useCase.execute(itemId)
        );

        verify(budgetItemRepository, never())
                .deleteById(any());

        verifyNoInteractions(
                manualItemRepository,
                budgetAccessService
        );
    }

    @Test
    void shouldNotDeleteItemWhenBudgetIsNotAccessible() {

        UUID itemId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        BudgetItem item = BudgetItem.createSinapi(
                groupId,
                compositionId,
                BigDecimal.ONE,
                new BigDecimal("100.00")
        );

        when(budgetItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        doThrow(new BudgetNotFoundException())
                .when(budgetAccessService)
                .getBudgetGroupForCurrentUser(groupId);

        assertThrows(
                BudgetNotFoundException.class,
                () -> useCase.execute(itemId)
        );

        verify(budgetItemRepository, never())
                .deleteById(any());

        verifyNoInteractions(manualItemRepository);
    }
}