package com.tomazbr9.buildprice.budget.application.usecase;

import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.SinapiCatalogGateway;
import com.tomazbr9.buildprice.budget.application.service.BudgetAccessService;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBudgetStructureUseCaseImplTest {

    @Mock
    private BudgetAccessService budgetAccessService;

    @Mock
    private BudgetGroupRepository budgetGroupRepository;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private ManualItemRepository manualItemRepository;

    @Mock
    private SinapiCatalogGateway sinapiCatalogGateway;

    private GetBudgetStructureUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetBudgetStructureUseCaseImpl(
                budgetAccessService,
                budgetGroupRepository,
                budgetItemRepository,
                manualItemRepository,
                sinapiCatalogGateway
        );
    }

    @Test
    void shouldBuildStructureWithSinapiItem() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

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
                        "Fundação",
                        0
                );

        BudgetItem item =
                BudgetItem.createSinapi(
                        groupId,
                        compositionId,
                        new BigDecimal("2"),
                        new BigDecimal("100.00")
                );

        SinapiCatalogGateway.CompositionData composition =
                new SinapiCatalogGateway.CompositionData(
                        compositionId,
                        versionId,
                        "12345",
                        "Concreto estrutural",
                        "M3",
                        new BigDecimal("999.99")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(group)
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(groupId)
                        )
        ).thenReturn(
                List.of(item)
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(
                                List.of(compositionId)
                        )
        ).thenReturn(
                List.of(composition)
        );

        when(
                manualItemRepository
                        .findByIds(List.of())
        ).thenReturn(
                List.of()
        );

        BudgetStructureResult result =
                useCase.execute(budgetId);

        assertEquals(
                1,
                result.groups().size()
        );

        var resultItem =
                result.groups()
                        .getFirst()
                        .items()
                        .getFirst();

        assertEquals(
                "12345",
                resultItem.code()
        );

        assertEquals(
                "Concreto estrutural",
                resultItem.description()
        );

        assertEquals(
                "M3",
                resultItem.unit()
        );

        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(
                                resultItem.unitPrice()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("200.00")
                        .compareTo(
                                resultItem.directCost()
                        )
        );
    }

    @Test
    void shouldUseSnapshotInsteadOfCurrentCompositionPrice() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
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

        BudgetItem item =
                BudgetItem.createSinapi(
                        groupId,
                        compositionId,
                        BigDecimal.ONE,
                        new BigDecimal("100.00")
                );

        SinapiCatalogGateway.CompositionData composition =
                new SinapiCatalogGateway.CompositionData(
                        compositionId,
                        versionId,
                        "98765",
                        "Serviço SINAPI",
                        "M2",
                        new BigDecimal("250.00")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(group)
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(groupId)
                        )
        ).thenReturn(
                List.of(item)
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(
                                List.of(compositionId)
                        )
        ).thenReturn(
                List.of(composition)
        );

        when(
                manualItemRepository
                        .findByIds(List.of())
        ).thenReturn(List.of());

        BudgetStructureResult result =
                useCase.execute(budgetId);

        var resultItem =
                result.groups()
                        .getFirst()
                        .items()
                        .getFirst();

        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(
                                resultItem.unitPrice()
                        )
        );

        assertNotEquals(
                0,
                composition.unitCost()
                        .compareTo(
                                resultItem.unitPrice()
                        )
        );
    }

    @Test
    void shouldBuildStructureWithManualItem() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID manualItemId = UUID.randomUUID();

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

        ManualItem manualItem =
                ManualItem.restore(
                        manualItemId,
                        budgetId,
                        "Transporte especial",
                        "UN",
                        new BigDecimal("150.00")
                );

        BudgetItem item =
                BudgetItem.createManual(
                        groupId,
                        manualItemId,
                        new BigDecimal("2"),
                        new BigDecimal("150.00")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(group)
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(groupId)
                        )
        ).thenReturn(
                List.of(item)
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(List.of())
        ).thenReturn(
                List.of()
        );

        when(
                manualItemRepository
                        .findByIds(
                                List.of(manualItemId)
                        )
        ).thenReturn(
                List.of(manualItem)
        );

        BudgetStructureResult result =
                useCase.execute(budgetId);

        var resultItem =
                result.groups()
                        .getFirst()
                        .items()
                        .getFirst();

        assertNull(
                resultItem.code()
        );

        assertEquals(
                "Transporte especial",
                resultItem.description()
        );

        assertEquals(
                "UN",
                resultItem.unit()
        );

        assertEquals(
                0,
                new BigDecimal("300.00")
                        .compareTo(
                                resultItem.directCost()
                        )
        );
    }

    @Test
    void shouldCalculateGroupAndBudgetTotalsWithBdi() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        UUID group1Id = UUID.randomUUID();
        UUID group2Id = UUID.randomUUID();

        UUID compositionId = UUID.randomUUID();
        UUID manualItemId = UUID.randomUUID();

        Budget budget =
                Budget.restore(
                        budgetId,
                        projectId,
                        versionId,
                        BudgetStatus.DRAFT,
                        new BigDecimal("20.00")
                );

        BudgetGroup group1 =
                BudgetGroup.restore(
                        group1Id,
                        budgetId,
                        null,
                        "Fundação",
                        0
                );

        BudgetGroup group2 =
                BudgetGroup.restore(
                        group2Id,
                        budgetId,
                        null,
                        "Acabamento",
                        1
                );

        BudgetItem sinapiItem =
                BudgetItem.createSinapi(
                        group1Id,
                        compositionId,
                        new BigDecimal("2"),
                        new BigDecimal("100.00")
                );

        BudgetItem manualBudgetItem =
                BudgetItem.createManual(
                        group2Id,
                        manualItemId,
                        new BigDecimal("3"),
                        new BigDecimal("100.00")
                );

        ManualItem manualItem =
                ManualItem.restore(
                        manualItemId,
                        budgetId,
                        "Serviço manual",
                        "UN",
                        new BigDecimal("100.00")
                );

        SinapiCatalogGateway.CompositionData composition =
                new SinapiCatalogGateway.CompositionData(
                        compositionId,
                        versionId,
                        "1000",
                        "Composição",
                        "M2",
                        new BigDecimal("100.00")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(
                        group1,
                        group2
                )
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(
                                        group1Id,
                                        group2Id
                                )
                        )
        ).thenReturn(
                List.of(
                        sinapiItem,
                        manualBudgetItem
                )
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(
                                List.of(compositionId)
                        )
        ).thenReturn(
                List.of(composition)
        );

        when(
                manualItemRepository
                        .findByIds(
                                List.of(manualItemId)
                        )
        ).thenReturn(
                List.of(manualItem)
        );

        BudgetStructureResult result =
                useCase.execute(budgetId);

        assertEquals(
                0,
                new BigDecimal("200.00")
                        .compareTo(
                                result.groups()
                                        .get(0)
                                        .directCost()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("300.00")
                        .compareTo(
                                result.groups()
                                        .get(1)
                                        .directCost()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("500.00")
                        .compareTo(
                                result.directCost()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(
                                result.bdiValue()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("600.00")
                        .compareTo(
                                result.finalPrice()
                        )
        );
    }

    @Test
    void shouldReturnZeroTotalsWhenBudgetHasNoItems() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
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
                        "Fundação",
                        0
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(group)
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(groupId)
                        )
        ).thenReturn(
                List.of()
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(List.of())
        ).thenReturn(List.of());

        when(
                manualItemRepository
                        .findByIds(List.of())
        ).thenReturn(List.of());

        BudgetStructureResult result =
                useCase.execute(budgetId);

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.directCost()
                )
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.bdiValue()
                )
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.finalPrice()
                )
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.groups()
                                .getFirst()
                                .directCost()
                )
        );
    }

    @Test
    void shouldThrowWhenReferencedCompositionIsMissing() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
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

        BudgetItem item =
                BudgetItem.createSinapi(
                        groupId,
                        compositionId,
                        BigDecimal.ONE,
                        new BigDecimal("100.00")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(group)
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(groupId)
                        )
        ).thenReturn(
                List.of(item)
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(
                                List.of(compositionId)
                        )
        ).thenReturn(
                List.of()
        );

        when(
                manualItemRepository
                        .findByIds(List.of())
        ).thenReturn(List.of());

        assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(budgetId)
        );
    }

    @Test
    void shouldThrowWhenReferencedManualItemIsMissing() {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID manualItemId = UUID.randomUUID();

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

        BudgetItem item =
                BudgetItem.createManual(
                        groupId,
                        manualItemId,
                        BigDecimal.ONE,
                        new BigDecimal("50.00")
                );

        when(
                budgetAccessService
                        .getBudgetForCurrentUser(budgetId)
        ).thenReturn(budget);

        when(
                budgetGroupRepository
                        .findByBudgetId(budgetId)
        ).thenReturn(
                List.of(group)
        );

        when(
                budgetItemRepository
                        .findByBudgetGroupIds(
                                List.of(groupId)
                        )
        ).thenReturn(
                List.of(item)
        );

        when(
                sinapiCatalogGateway
                        .findCompositionsByIds(List.of())
        ).thenReturn(List.of());

        when(
                manualItemRepository
                        .findByIds(
                                List.of(manualItemId)
                        )
        ).thenReturn(
                List.of()
        );

        assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(budgetId)
        );
    }
}