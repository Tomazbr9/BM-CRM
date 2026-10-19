package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.CreateBudgetGroupCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetGroupResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.InvalidParentBudgetGroupException;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBudgetGroupUseCaseImplTest {

    @Mock
    private BudgetGroupRepository budgetGroupRepository;

    @Mock
    private BudgetAccessService budgetAccessService;

    @Mock
    private BudgetRepository budgetRepository;

    private CreateBudgetGroupUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBudgetGroupUseCaseImpl(
                budgetGroupRepository,
                budgetAccessService,
                budgetRepository
        );
    }

    @Test
    void shouldCreateRootBudgetGroup() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        new BigDecimal("20.00")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .save(any(BudgetGroup.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        CreateBudgetGroupCommand command =
                new CreateBudgetGroupCommand(
                        budgetId,
                        null,
                        "Fundação",
                        0
                );

        BudgetGroupResult result =
                useCase.execute(command);

        assertNotNull(result.id());

        assertEquals(
                budgetId,
                result.budgetId()
        );

        assertNull(
                result.parentGroupId()
        );

        assertEquals(
                "Fundação",
                result.name()
        );

        assertEquals(
                0,
                result.sortOrder()
        );

        verify(
                budgetAccessService
        ).getBudgetForCurrentUser(budgetId);

        verify(
                budgetGroupRepository
        ).save(any(BudgetGroup.class));
    }

    @Test
    void shouldCreateChildBudgetGroup() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID parentGroupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup parent =
                BudgetGroup.restore(
                        parentGroupId,
                        budgetId,
                        null,
                        "Fundação",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findById(parentGroupId)
        ).thenReturn(
                Optional.of(parent)
        );

        when(
                budgetGroupRepository
                        .save(any(BudgetGroup.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        CreateBudgetGroupCommand command =
                new CreateBudgetGroupCommand(
                        budgetId,
                        parentGroupId,
                        "Escavação",
                        1
                );

        BudgetGroupResult result =
                useCase.execute(command);

        assertEquals(
                parentGroupId,
                result.parentGroupId()
        );

        assertEquals(
                "Escavação",
                result.name()
        );

        assertEquals(
                1,
                result.sortOrder()
        );
    }

    @Test
    void shouldNotPersistWhenBudgetAccessFails() {

        UUID budgetId =
                UUID.randomUUID();

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenThrow(
                new BudgetNotFoundException()
        );

        CreateBudgetGroupCommand command =
                new CreateBudgetGroupCommand(
                        budgetId,
                        null,
                        "Fundação",
                        0
                );

        assertThrows(
                BudgetNotFoundException.class,
                () -> useCase.execute(command)
        );

        verify(
                budgetGroupRepository,
                never()
        ).save(any());

        verify(
                budgetGroupRepository,
                never()
        ).findById(any());
    }

    @Test
    void shouldThrowWhenParentGroupDoesNotExist() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID parentGroupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findById(parentGroupId)
        ).thenReturn(
                Optional.empty()
        );

        CreateBudgetGroupCommand command =
                new CreateBudgetGroupCommand(
                        budgetId,
                        parentGroupId,
                        "Escavação",
                        1
                );

        assertThrows(
                InvalidParentBudgetGroupException.class,
                () -> useCase.execute(command)
        );

        verify(
                budgetGroupRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenParentGroupBelongsToAnotherBudget() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        UUID otherBudgetId =
                UUID.randomUUID();

        UUID parentGroupId =
                UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup parent =
                BudgetGroup.restore(
                        parentGroupId,
                        otherBudgetId,
                        null,
                        "Outro Grupo",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findById(parentGroupId)
        ).thenReturn(
                Optional.of(parent)
        );

        CreateBudgetGroupCommand command =
                new CreateBudgetGroupCommand(
                        budgetId,
                        parentGroupId,
                        "Grupo Inválido",
                        0
                );

        assertThrows(
                InvalidParentBudgetGroupException.class,
                () -> useCase.execute(command)
        );

        verify(
                budgetGroupRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldPersistGroupWithCorrectBudgetAndParent() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID parentGroupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup parent =
                BudgetGroup.restore(
                        parentGroupId,
                        budgetId,
                        null,
                        "Fundação",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findById(parentGroupId)
        ).thenReturn(
                Optional.of(parent)
        );

        when(
                budgetGroupRepository
                        .save(any(BudgetGroup.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        useCase.execute(
                new CreateBudgetGroupCommand(
                        budgetId,
                        parentGroupId,
                        "Concreto",
                        2
                )
        );

        ArgumentCaptor<BudgetGroup> captor =
                ArgumentCaptor.forClass(
                        BudgetGroup.class
                );

        verify(
                budgetGroupRepository
        ).save(captor.capture());

        BudgetGroup saved =
                captor.getValue();

        assertEquals(
                budgetId,
                saved.getBudgetId()
        );

        assertEquals(
                parentGroupId,
                saved.getParentGroupId()
        );

        assertEquals(
                "Concreto",
                saved.getName()
        );

        assertEquals(
                2,
                saved.getSortOrder()
        );
    }
}