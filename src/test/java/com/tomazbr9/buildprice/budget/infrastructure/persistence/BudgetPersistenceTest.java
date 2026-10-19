package com.tomazbr9.buildprice.budget.infrastructure.persistence;

import com.tomazbr9.buildprice.budget.application.port.out.BudgetRepository;
import com.tomazbr9.buildprice.budget.domain.entity.Budget;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(BudgetJpaRepositoryAdapter.class)
class BudgetPersistenceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistBudgetWithProjectAndSinapiVersion() {

        UUID budgetId = UUID.randomUUID();

        Budget budget = Budget.restore(
                budgetId,
                projectId,
                versionId,
                BudgetStatus.DRAFT,
                new BigDecimal("20.00")
        );

        Budget saved = budgetRepository.save(budget);

        entityManager.flush();
        entityManager.clear();

        Budget found = budgetRepository
                .findById(saved.getId())
                .orElseThrow();

        assertEquals(budgetId, found.getId());
        assertEquals(projectId, found.getProjectId());
        assertEquals(versionId, found.getSinapiTableVersionId());
        assertEquals(BudgetStatus.DRAFT, found.getStatus());

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(found.getBdiPercentage())
        );
    }
}