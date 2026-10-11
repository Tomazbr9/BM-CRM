package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.AddManualBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetGroupNotFoundException;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ProjectGateway;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import com.tomazbr9.buildprice.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddManualBudgetItemUseCaseImplTest {

    @Mock
    private BudgetGroupRepository budgetGroupRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ManualItemRepository manualItemRepository;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private ProjectGateway projectGateway;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private BudgetAccessService budgetAccessService;

    private AddManualBudgetItemUseCaseImpl useCase;

    @BeforeEach
    void setUp() {

        useCase = new AddManualBudgetItemUseCaseImpl(
                budgetGroupRepository,
                budgetRepository,
                manualItemRepository,
                budgetItemRepository,
                projectGateway,
                currentUserProvider,
                budgetAccessService
        );
    }

    @Test
    void shouldCreateManualItemAndBudgetItem() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        new BigDecimal("20.00")
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Acabamento",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetGroupForCurrentUser(groupId)
        ).thenReturn(
                new BudgetAccessService.BudgetGroupAccess(
                        budget,
                        group
                )
        );

        when(
                manualItemRepository
                        .save(any(ManualItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        BudgetItemResult result =
                useCase.execute(
                        new AddManualBudgetItemCommand(
                                groupId,
                                "Instalação personalizada",
                                "UN",
                                new BigDecimal("80.00"),
                                new BigDecimal("4")
                        )
                );

        assertEquals(
                BudgetItemType.MANUAL,
                result.type()
        );

        assertNull(
                result.compositionId()
        );

        assertNotNull(
                result.manualItemId()
        );

        assertEquals(
                0,
                new BigDecimal("80.00")
                        .compareTo(
                                result.unitPriceSnapshot()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("320.00")
                        .compareTo(
                                result.directCost()
                        )
        );

        verify(
                manualItemRepository
        ).save(any(ManualItem.class));

        verify(
                budgetItemRepository
        ).save(any(BudgetItem.class));
    }

    @Test
    void shouldCopyManualItemPriceToSnapshot() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Serviços",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetGroupForCurrentUser(groupId)
        ).thenReturn(
                new BudgetAccessService.BudgetGroupAccess(
                        budget,
                        group
                )
        );

        when(
                manualItemRepository
                        .save(any(ManualItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        useCase.execute(
                new AddManualBudgetItemCommand(
                        groupId,
                        "Serviço Especial",
                        "H",
                        new BigDecimal("150.00"),
                        new BigDecimal("2")
                )
        );

        ArgumentCaptor<BudgetItem> captor =
                ArgumentCaptor.forClass(
                        BudgetItem.class
                );

        verify(
                budgetItemRepository
        ).save(captor.capture());

        BudgetItem item =
                captor.getValue();

        assertEquals(
                BudgetItemType.MANUAL,
                item.getType()
        );

        assertNull(
                item.getCompositionId()
        );

        assertNotNull(
                item.getManualItemId()
        );

        assertEquals(
                0,
                new BigDecimal("150.00")
                        .compareTo(
                                item.getUnitPriceSnapshot()
                        )
        );
    }

    @Test
    void shouldAssociateManualItemWithCorrectBudget() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Serviços",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetGroupForCurrentUser(groupId)
        ).thenReturn(
                new BudgetAccessService.BudgetGroupAccess(
                        budget,
                        group
                )
        );

        when(
                manualItemRepository
                        .save(any(ManualItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        useCase.execute(
                new AddManualBudgetItemCommand(
                        groupId,
                        "Item Teste",
                        "UN",
                        new BigDecimal("50.00"),
                        BigDecimal.ONE
                )
        );

        ArgumentCaptor<ManualItem> captor =
                ArgumentCaptor.forClass(
                        ManualItem.class
                );

        verify(
                manualItemRepository
        ).save(captor.capture());

        ManualItem manualItem =
                captor.getValue();

        assertEquals(
                budgetId,
                manualItem.getBudgetId()
        );

        assertEquals(
                "Item Teste",
                manualItem.getDescription()
        );

        assertEquals(
                "UN",
                manualItem.getUnit()
        );

        assertEquals(
                0,
                new BigDecimal("50.00")
                        .compareTo(
                                manualItem.getUnitPrice()
                        )
        );
    }

    @Test
    void shouldUseValidatedGroupReturnedByBudgetAccessService() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        UUID.randomUUID(),
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Serviços",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetGroupForCurrentUser(groupId)
        ).thenReturn(
                new BudgetAccessService.BudgetGroupAccess(
                        budget,
                        group
                )
        );

        when(
                manualItemRepository
                        .save(any(ManualItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        useCase.execute(
                new AddManualBudgetItemCommand(
                        groupId,
                        "Item",
                        "UN",
                        BigDecimal.ONE,
                        BigDecimal.ONE
                )
        );

        verify(
                budgetAccessService
        ).getBudgetGroupForCurrentUser(
                groupId
        );
    }

    @Test
    void shouldNotPersistWhenBudgetAccessFails() {

        UUID groupId =
                UUID.randomUUID();

        when(
                budgetAccessService
                        .getBudgetGroupForCurrentUser(groupId)
        ).thenThrow(
                new BudgetGroupNotFoundException()
        );

        AddManualBudgetItemCommand command =
                new AddManualBudgetItemCommand(
                        groupId,
                        "Item",
                        "UN",
                        BigDecimal.ONE,
                        BigDecimal.ONE
                );

        assertThrows(
                BudgetGroupNotFoundException.class,
                () -> useCase.execute(command)
        );

        verifyNoInteractions(
                manualItemRepository
        );

        verifyNoInteractions(
                budgetItemRepository
        );
    }
}