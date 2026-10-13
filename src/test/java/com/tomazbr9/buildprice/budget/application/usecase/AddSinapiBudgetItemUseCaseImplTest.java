package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.command.AddSinapiBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.exception.BudgetGroupNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.CompositionNotAvailableForBudgetException;
import com.tomazbr9.buildprice.budget.application.port.out.*;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddSinapiBudgetItemUseCaseImplTest {

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private SinapiCatalogGateway sinapiCatalogGateway;

    @Mock
    private BudgetAccessService budgetAccessService;

    @Mock
    private BudgetGroupRepository budgetGroupRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ProjectGateway projectGateway;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private AddSinapiBudgetItemUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddSinapiBudgetItemUseCaseImpl(
                budgetGroupRepository,
                budgetRepository,
                budgetItemRepository,
                projectGateway,
                sinapiCatalogGateway,
                currentUserProvider,
                budgetAccessService
        );
    }

    @Test
    void shouldAddSinapiItemUsingCompositionPriceAsSnapshot() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        BigDecimal unitCost =
                new BigDecimal("125.50");

        BigDecimal quantity =
                new BigDecimal("3");

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        versionId,
                        BudgetStatus.DRAFT,
                        new BigDecimal("20.00")
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Estrutura",
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
                sinapiCatalogGateway
                        .findCompositionById(compositionId)
        ).thenReturn(
                Optional.of(
                        new SinapiCatalogGateway.CompositionData(
                                compositionId,
                                versionId,
                                "12345",
                                "Concreto estrutural",
                                "M3",
                                unitCost
                        )
                )
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        BudgetItemResult result =
                useCase.execute(
                        new AddSinapiBudgetItemCommand(
                                groupId,
                                compositionId,
                                quantity
                        )
                );

        assertEquals(
                BudgetItemType.SINAPI,
                result.type()
        );

        assertEquals(
                compositionId,
                result.compositionId()
        );

        assertNull(
                result.manualItemId()
        );

        assertEquals(
                0,
                unitCost.compareTo(
                        result.unitPriceSnapshot()
                )
        );

        assertEquals(
                0,
                new BigDecimal("376.50")
                        .compareTo(
                                result.directCost()
                        )
        );
    }

    @Test
    void shouldPersistCompositionPriceAsSnapshot() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        BigDecimal unitCost =
                new BigDecimal("250.00");

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        versionId,
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Fundação",
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
                sinapiCatalogGateway
                        .findCompositionById(compositionId)
        ).thenReturn(
                Optional.of(
                        new SinapiCatalogGateway.CompositionData(
                                compositionId,
                                versionId,
                                "10001",
                                "Composição teste",
                                "M2",
                                unitCost
                        )
                )
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        useCase.execute(
                new AddSinapiBudgetItemCommand(
                        groupId,
                        compositionId,
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
                BudgetItemType.SINAPI,
                item.getType()
        );

        assertEquals(
                compositionId,
                item.getCompositionId()
        );

        assertNull(
                item.getManualItemId()
        );

        assertEquals(
                0,
                unitCost.compareTo(
                        item.getUnitPriceSnapshot()
                )
        );
    }

    @Test
    void shouldRejectCompositionFromDifferentSinapiVersion() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        UUID budgetVersionId =
                UUID.randomUUID();

        UUID otherVersionId =
                UUID.randomUUID();

        UUID compositionId =
                UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        budgetVersionId,
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Estrutura",
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
                sinapiCatalogGateway
                        .findCompositionById(compositionId)
        ).thenReturn(
                Optional.of(
                        new SinapiCatalogGateway.CompositionData(
                                compositionId,
                                otherVersionId,
                                "12345",
                                "Composição de outra versão",
                                "M2",
                                new BigDecimal("100.00")
                        )
                )
        );

        assertThrows(
                CompositionNotAvailableForBudgetException.class,
                () -> useCase.execute(
                        new AddSinapiBudgetItemCommand(
                                groupId,
                                compositionId,
                                BigDecimal.ONE
                        )
                )
        );

        verify(
                budgetItemRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenCompositionDoesNotExist() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

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
                        "Estrutura",
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
                sinapiCatalogGateway
                        .findCompositionById(compositionId)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                CompositionNotAvailableForBudgetException.class,
                () -> useCase.execute(
                        new AddSinapiBudgetItemCommand(
                                groupId,
                                compositionId,
                                BigDecimal.ONE
                        )
                )
        );

        verify(
                budgetItemRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldUseValidatedBudgetAndGroupFromAccessService() {

        UUID projectId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        versionId,
                        BudgetStatus.DRAFT,
                        BigDecimal.ZERO
                );

        BudgetGroup group =
                BudgetGroup.restore(
                        groupId,
                        budgetId,
                        null,
                        "Estrutura",
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
                sinapiCatalogGateway
                        .findCompositionById(compositionId)
        ).thenReturn(
                Optional.of(
                        new SinapiCatalogGateway.CompositionData(
                                compositionId,
                                versionId,
                                "99999",
                                "Composição teste",
                                "UN",
                                new BigDecimal("50.00")
                        )
                )
        );

        when(
                budgetItemRepository
                        .save(any(BudgetItem.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        useCase.execute(
                new AddSinapiBudgetItemCommand(
                        groupId,
                        compositionId,
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
    void shouldNotQueryCatalogOrPersistWhenBudgetAccessFails() {

        UUID groupId =
                UUID.randomUUID();

        UUID compositionId =
                UUID.randomUUID();

        when(
                budgetAccessService
                        .getBudgetGroupForCurrentUser(groupId)
        ).thenThrow(
                new BudgetGroupNotFoundException()
        );

        AddSinapiBudgetItemCommand command =
                new AddSinapiBudgetItemCommand(
                        groupId,
                        compositionId,
                        BigDecimal.ONE
                );

        assertThrows(
                BudgetGroupNotFoundException.class,
                () -> useCase.execute(command)
        );

        verifyNoInteractions(
                sinapiCatalogGateway
        );

        verifyNoInteractions(
                budgetItemRepository
        );
    }
}