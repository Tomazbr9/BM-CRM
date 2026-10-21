
package com.tomazbr9.buildprice.reports;

import com.tomazbr9.buildprice.shared.security.CurrentUserProvider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@WithMockUser(username = "test-user", roles = "USER")
@Sql(scripts = {
        "classpath:sql/budget-test-data.sql",
        "classpath:sql/reports-integration-data.sql"
})
class ReportsIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString(
            "33333333-3333-3333-3333-333333333333"
    );

    private static final UUID OTHER_USER_ID = UUID.fromString(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    );

    private static final UUID BUDGET_ID = UUID.fromString(
            "55555555-5555-5555-5555-555555555555"
    );

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("buildprice_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.flyway.enabled",
                () -> true
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void shouldGenerateAbcCurveForBudgetOwner()
            throws Exception {

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(OWNER_ID);

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/abc-curve",
                                BUDGET_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId")
                        .value(BUDGET_ID.toString()))
                .andExpect(jsonPath("$.totalDirectCost")
                        .value(174.90))
                .andExpect(jsonPath("$.items.length()")
                        .value(1))
                .andExpect(jsonPath("$.items[0].directCost")
                        .value(174.90))
                .andExpect(jsonPath("$.items[0].classification")
                        .value("A"))
                .andExpect(jsonPath("$.items[0].accumulatedPercentage")
                        .value(100.00));
    }

    @Test
    void shouldRejectAccessToAnotherUsersBudget()
            throws Exception {

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(OTHER_USER_ID);

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/abc-curve",
                                BUDGET_ID
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Orçamento não encontrado"));
    }

    @Test
    void shouldReturnNotFoundForNonexistentBudget()
            throws Exception {

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(OWNER_ID);

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/abc-curve",
                                UUID.randomUUID()
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Orçamento não encontrado"));
    }

    @Test
    void shouldNotModifyBudgetDuringReportGeneration()
            throws Exception {

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(OWNER_ID);

        Integer before = countBudgetItems();

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/abc-curve",
                                BUDGET_ID
                        )
                )
                .andExpect(status().isOk());

        Integer after = countBudgetItems();

        assertEquals(before, after);
        assertEquals(1, after);
    }

    private Integer countBudgetItems() {
        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tb_budget_items
                WHERE budget_group_id = ?
                """,
                Integer.class,
                UUID.fromString(
                        "66666666-6666-6666-6666-666666666666"
                )
        );
    }
}
