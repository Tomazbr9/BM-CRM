package com.tomazbr9.buildprice.budget.infrastructure.persistence;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetGroupRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetItemRepository;
import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.application.port.out.ManualItemRepository;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetGroup;
import com.tomazbr9.buildprice.budget.domain.entity.BudgetItem;
import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
        BudgetJpaRepositoryAdapter.class,
        BudgetGroupJpaRepositoryAdapter.class,
        BudgetItemJpaRepositoryAdapter.class,
        ManualItemJpaRepositoryAdapter.class
})
@Sql(
        scripts = "/sql/budget-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class BudgetPersistenceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BudgetItemRepository budgetItemRepository;

    @Autowired
    private ManualItemRepository manualItemRepository;

    @Autowired
    private BudgetGroupRepository budgetGroupRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistBudgetWithProjectAndSinapiVersion() {

        UUID projectId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID versionId = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        Budget budget = Budget.create(
                projectId,
                versionId,
                new BigDecimal("20.00")
        );

        Budget saved = budgetRepository.save(budget);

        entityManager.flush();
        entityManager.clear();

        Budget found = budgetRepository
                .findById(saved.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(saved.getId(), found.getId()),
                () -> assertEquals(projectId, found.getProjectId()),
                () -> assertEquals(versionId, found.getSinapiTableVersionId()),
                () -> assertEquals(BudgetStatus.DRAFT, found.getStatus()),
                () -> assertEquals(
                        0,
                        new BigDecimal("20.00")
                                .compareTo(found.getBdiPercentage())
                )
        );
    }

    @Test
    void shouldPersistBudgetGroupsAndSubgroups() {

        UUID projectId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID versionId = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        Budget budget = Budget.create(
                projectId,
                versionId,
                new BigDecimal("20.00")
        );

        budgetRepository.save(budget);

        BudgetGroup rootGroup = BudgetGroup.create(
                budget.getId(),
                null,
                "Fundação",
                0
        );

        BudgetGroup savedRoot = budgetGroupRepository.save(rootGroup);

        BudgetGroup childGroup = BudgetGroup.create(
                budget.getId(),
                savedRoot.getId(),
                "Escavação",
                1
        );

        BudgetGroup savedChild = budgetGroupRepository.save(childGroup);

        entityManager.flush();
        entityManager.clear();

        BudgetGroup foundRoot = budgetGroupRepository
                .findById(savedRoot.getId())
                .orElseThrow();

        BudgetGroup foundChild = budgetGroupRepository
                .findById(savedChild.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(budget.getId(), foundRoot.getBudgetId()),
                () -> assertNull(foundRoot.getParentGroupId()),
                () -> assertEquals("Fundação", foundRoot.getName()),
                () -> assertEquals(0, foundRoot.getSortOrder()),

                () -> assertEquals(budget.getId(), foundChild.getBudgetId()),
                () -> assertEquals(savedRoot.getId(), foundChild.getParentGroupId()),
                () -> assertEquals("Escavação", foundChild.getName()),
                () -> assertEquals(1, foundChild.getSortOrder())
        );

        List<BudgetGroup> groups = budgetGroupRepository
                .findByBudgetId(budget.getId());

        assertEquals(2, groups.size());

        List<BudgetGroup> children = budgetGroupRepository
                .findByParentGroupId(savedRoot.getId());

        assertEquals(1, children.size());
        assertEquals(savedChild.getId(), children.getFirst().getId());
    }

    @Test
    void shouldPersistSinapiAndManualBudgetItems() {

        UUID projectId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID versionId = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        UUID compositionId = UUID.fromString(
                "44444444-4444-4444-4444-444444444444"
        );

        Budget budget = budgetRepository.save(
                Budget.create(
                        projectId,
                        versionId,
                        new BigDecimal("20.00")
                )
        );

        BudgetGroup group = budgetGroupRepository.save(
                BudgetGroup.create(
                        budget.getId(),
                        null,
                        "Alvenaria",
                        0
                )
        );

        BudgetItem sinapiItem = budgetItemRepository.save(
                BudgetItem.createSinapi(
                        group.getId(),
                        compositionId,
                        new BigDecimal("10.00"),
                        new BigDecimal("87.45")
                )
        );

        ManualItem manualItem = manualItemRepository.save(
                ManualItem.create(
                        budget.getId(),
                        "Transporte especial",
                        "UN",
                        new BigDecimal("150.00")
                )
        );

        BudgetItem manualBudgetItem = budgetItemRepository.save(
                BudgetItem.createManual(
                        group.getId(),
                        manualItem.getId(),
                        new BigDecimal("2.00"),
                        manualItem.getUnitPrice()
                )
        );

        entityManager.flush();
        entityManager.clear();

        BudgetItem foundSinapi = budgetItemRepository
                .findById(sinapiItem.getId())
                .orElseThrow();

        BudgetItem foundManual = budgetItemRepository
                .findById(manualBudgetItem.getId())
                .orElseThrow();

        ManualItem foundManualRecord = manualItemRepository
                .findById(manualItem.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        BudgetItemType.SINAPI,
                        foundSinapi.getType()
                ),
                () -> assertEquals(
                        compositionId,
                        foundSinapi.getCompositionId()
                ),
                () -> assertEquals(
                        0,
                        new BigDecimal("874.50")
                                .compareTo(foundSinapi.calculateDirectCost())
                ),
                () -> assertEquals(
                        BudgetItemType.MANUAL,
                        foundManual.getType()
                ),
                () -> assertEquals(
                        manualItem.getId(),
                        foundManual.getManualItemId()
                ),
                () -> assertEquals(
                        "Transporte especial",
                        foundManualRecord.getDescription()
                ),
                () -> assertEquals(
                        0,
                        new BigDecimal("300.00")
                                .compareTo(foundManual.calculateDirectCost())
                )
        );

        List<BudgetItem> groupItems = budgetItemRepository
                .findByBudgetGroupId(group.getId());

        assertEquals(2, groupItems.size());
    }


    @Test
    void shouldDeleteManualBudgetItemAndItsManualRecord() {

        UUID projectId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID versionId = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        Budget budget = budgetRepository.save(
                Budget.create(
                        projectId,
                        versionId,
                        new BigDecimal("20.00")
                )
        );

        BudgetGroup group = budgetGroupRepository.save(
                BudgetGroup.create(
                        budget.getId(),
                        null,
                        "Serviços complementares",
                        0
                )
        );

        ManualItem manualItem = manualItemRepository.save(
                ManualItem.create(
                        budget.getId(),
                        "Transporte especial",
                        "UN",
                        new BigDecimal("150.00")
                )
        );

        BudgetItem budgetItem = budgetItemRepository.save(
                BudgetItem.createManual(
                        group.getId(),
                        manualItem.getId(),
                        new BigDecimal("2.00"),
                        manualItem.getUnitPrice()
                )
        );

        entityManager.flush();
        entityManager.clear();

        // Exclui primeiro o vínculo do orçamento.
        budgetItemRepository.deleteById(budgetItem.getId());

        // Garante que o DELETE chegue ao PostgreSQL
        // antes da exclusão do registro manual.
        entityManager.flush();

        manualItemRepository.deleteById(manualItem.getId());

        entityManager.flush();
        entityManager.clear();

        assertTrue(
                budgetItemRepository.findById(budgetItem.getId()).isEmpty()
        );

        assertTrue(
                manualItemRepository.findById(manualItem.getId()).isEmpty()
        );
    }


    @Test
    void shouldDeleteSinapiBudgetItemWithoutDeletingComposition() {

        UUID projectId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID versionId = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        UUID compositionId = UUID.fromString(
                "44444444-4444-4444-4444-444444444444"
        );

        Budget budget = budgetRepository.save(
                Budget.create(
                        projectId,
                        versionId,
                        new BigDecimal("20.00")
                )
        );

        BudgetGroup group = budgetGroupRepository.save(
                BudgetGroup.create(
                        budget.getId(),
                        null,
                        "Alvenaria",
                        0
                )
        );

        BudgetItem item = budgetItemRepository.save(
                BudgetItem.createSinapi(
                        group.getId(),
                        compositionId,
                        new BigDecimal("10.00"),
                        new BigDecimal("87.45")
                )
        );

        entityManager.flush();
        entityManager.clear();

        budgetItemRepository.deleteById(item.getId());

        entityManager.flush();
        entityManager.clear();

        assertTrue(
                budgetItemRepository.findById(item.getId()).isEmpty()
        );

        Long compositionCount = entityManager.createQuery(
                        """
                        SELECT COUNT(c)
                        FROM CompositionJpaEntity c
                        WHERE c.id = :compositionId
                        """,
                        Long.class
                )
                .setParameter("compositionId", compositionId)
                .getSingleResult();

        assertEquals(1L, compositionCount);
    }


}