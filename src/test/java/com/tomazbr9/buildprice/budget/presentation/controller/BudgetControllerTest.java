package com.tomazbr9.buildprice.budget.presentation.controller;

import com.tomazbr9.buildprice.budget.application.command.CreateBudgetCommand;
import com.tomazbr9.buildprice.budget.application.command.CreateBudgetGroupCommand;
import com.tomazbr9.buildprice.budget.application.dto.*;
import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.BudgetProjectNotFoundException;
import com.tomazbr9.buildprice.budget.application.exception.InvalidParentBudgetGroupException;
import com.tomazbr9.buildprice.budget.application.exception.SinapiTableVersionNotFoundException;
import com.tomazbr9.buildprice.budget.application.port.in.CreateBudgetGroupUseCase;
import com.tomazbr9.buildprice.budget.application.port.in.CreateBudgetUseCase;
import com.tomazbr9.buildprice.budget.application.port.in.GetBudgetStructureUseCase;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import com.tomazbr9.buildprice.budget.presentation.exception.BudgetExceptionHandler;
import com.tomazbr9.buildprice.identity.infrastructure.security.JwtAuthenticationFilter;
import com.tomazbr9.buildprice.identity.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@Import(BudgetExceptionHandler.class)
@WithMockUser(
        username = "test-user",
        roles = "USER"
)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CreateBudgetUseCase createBudgetUseCase;

    @MockitoBean
    private CreateBudgetGroupUseCase createBudgetGroupUseCase;

    @MockitoBean
    private GetBudgetStructureUseCase getBudgetStructureUseCase;

    @Test
    void shouldReturnBudgetStructure()
            throws Exception {

        UUID budgetId =
                UUID.randomUUID();

        UUID projectId =
                UUID.randomUUID();

        UUID versionId =
                UUID.randomUUID();

        UUID groupId =
                UUID.randomUUID();

        UUID itemId =
                UUID.randomUUID();

        UUID compositionId =
                UUID.randomUUID();

        BudgetStructureItemResult item =
                new BudgetStructureItemResult(
                        itemId,
                        BudgetItemType.SINAPI,
                        compositionId,
                        "12345",
                        "Concreto estrutural",
                        "M3",
                        new BigDecimal("2.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("200.00")
                );

        BudgetStructureGroupResult group =
                new BudgetStructureGroupResult(
                        groupId,
                        null,
                        "Fundação",
                        0,
                        new BigDecimal("200.00"),
                        List.of(item)
                );

        BudgetStructureResult result =
                new BudgetStructureResult(
                        budgetId,
                        projectId,
                        versionId,
                        BudgetStatus.DRAFT,
                        new BigDecimal("20.00"),
                        new BigDecimal("200.00"),
                        new BigDecimal("40.00"),
                        new BigDecimal("240.00"),
                        List.of(group)
                );

        when(
                getBudgetStructureUseCase
                        .execute(budgetId)
        ).thenReturn(result);

        mockMvc.perform(
                        get(
                                "/api/v1/budgets/{budgetId}",
                                budgetId
                        )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        "application/json"
                                )
                )

                .andExpect(
                        jsonPath("$.budgetId")
                                .value(
                                        budgetId.toString()
                                )
                )

                .andExpect(
                        jsonPath("$.projectId")
                                .value(
                                        projectId.toString()
                                )
                )

                .andExpect(
                        jsonPath("$.sinapiTableVersionId")
                                .value(
                                        versionId.toString()
                                )
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("DRAFT")
                )

                .andExpect(
                        jsonPath("$.bdiPercentage")
                                .value(20.00)
                )

                .andExpect(
                        jsonPath("$.directCost")
                                .value(200.00)
                )

                .andExpect(
                        jsonPath("$.bdiValue")
                                .value(40.00)
                )

                .andExpect(
                        jsonPath("$.finalPrice")
                                .value(240.00)
                )

                .andExpect(
                        jsonPath("$.groups.length()")
                                .value(1)
                )

                .andExpect(
                        jsonPath("$.groups[0].id")
                                .value(
                                        groupId.toString()
                                )
                )

                .andExpect(
                        jsonPath("$.groups[0].parentGroupId")
                                .doesNotExist()
                )

                .andExpect(
                        jsonPath("$.groups[0].name")
                                .value("Fundação")
                )

                .andExpect(
                        jsonPath("$.groups[0].sortOrder")
                                .value(0)
                )

                .andExpect(
                        jsonPath("$.groups[0].directCost")
                                .value(200.00)
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items.length()"
                        ).value(1)
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].id"
                        ).value(
                                itemId.toString()
                        )
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].type"
                        ).value("SINAPI")
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].referenceId"
                        ).value(
                                compositionId.toString()
                        )
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].code"
                        ).value("12345")
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].description"
                        ).value(
                                "Concreto estrutural"
                        )
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].unit"
                        ).value("M3")
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].quantity"
                        ).value(2.00)
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].unitPrice"
                        ).value(100.00)
                )

                .andExpect(
                        jsonPath(
                                "$.groups[0].items[0].directCost"
                        ).value(200.00)
                );

        verify(
                getBudgetStructureUseCase
        ).execute(budgetId);
    }

    @Test
    void shouldReturnNotFoundWhenBudgetDoesNotExist()
            throws Exception {

        UUID budgetId =
                UUID.randomUUID();

        when(
                getBudgetStructureUseCase
                        .execute(budgetId)
        ).thenThrow(
                new BudgetNotFoundException()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/budgets/{budgetId}",
                                budgetId
                        )
                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        "application/json"
                                )
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )

                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Orçamento não encontrado")
                )

                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/budgets/"
                                                + budgetId
                                )
                )

                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );

        verify(
                getBudgetStructureUseCase
        ).execute(budgetId);
    }

    @Test
    void shouldReturnBadRequestWhenBudgetIdIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/budgets/{budgetId}",
                                "invalid-uuid"
                        )
                )

                .andExpect(
                        status().isBadRequest()
                );

        verify(
                getBudgetStructureUseCase,
                never()
        ).execute(any());
    }

    @Test
    void shouldCreateBudget() throws Exception {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        BudgetResult result =
                new BudgetResult(
                        budgetId,
                        projectId,
                        versionId,
                        BudgetStatus.DRAFT,
                        new BigDecimal("20.00")
                );

        when(
                createBudgetUseCase.execute(
                        any(CreateBudgetCommand.class)
                )
        ).thenReturn(result);

        mockMvc.perform(
                        post("/api/v1/budgets")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "projectId": "%s",
                                      "sinapiTableVersionId": "%s"
                                    }
                                    """.formatted(
                                        projectId,
                                        versionId
                                ))
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        budgetId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.projectId")
                                .value(
                                        projectId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.sinapiTableVersionId")
                                .value(
                                        versionId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("DRAFT")
                )
                .andExpect(
                        jsonPath("$.bdiPercentage")
                                .value(20.00)
                );

        ArgumentCaptor<CreateBudgetCommand> captor =
                ArgumentCaptor.forClass(
                        CreateBudgetCommand.class
                );

        verify(
                createBudgetUseCase
        ).execute(captor.capture());

        CreateBudgetCommand command =
                captor.getValue();

        assertEquals(
                projectId,
                command.projectId()
        );

        assertEquals(
                versionId,
                command.sinapiTableVersionId()
        );
    }

    @Test
    void shouldReturnBadRequestWhenCreateBudgetRequestIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/budgets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "projectId": null,
                                      "sinapiTableVersionId": null
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                createBudgetUseCase,
                never()
        ).execute(any());
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExist()
            throws Exception {

        UUID projectId =
                UUID.randomUUID();

        UUID versionId =
                UUID.randomUUID();

        when(
                createBudgetUseCase.execute(
                        any(CreateBudgetCommand.class)
                )
        ).thenThrow(
                new BudgetProjectNotFoundException()
        );

        mockMvc.perform(
                        post("/api/v1/budgets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "projectId": "%s",
                                      "sinapiTableVersionId": "%s"
                                    }
                                    """.formatted(
                                        projectId,
                                        versionId
                                ))
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Projeto não encontrado")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/budgets"
                                )
                );
    }

    @Test
    void shouldReturnNotFoundWhenSinapiVersionDoesNotExist()
            throws Exception {

        UUID projectId =
                UUID.randomUUID();

        UUID versionId =
                UUID.randomUUID();

        when(
                createBudgetUseCase.execute(
                        any(CreateBudgetCommand.class)
                )
        ).thenThrow(
                new SinapiTableVersionNotFoundException()
        );

        mockMvc.perform(
                        post("/api/v1/budgets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "projectId": "%s",
                                      "sinapiTableVersionId": "%s"
                                    }
                                    """.formatted(
                                        projectId,
                                        versionId
                                ))
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Versão SINAPI não encontrada"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/budgets"
                                )
                );
    }

    @Test
    void shouldCreateRootBudgetGroup() throws Exception {

        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        BudgetGroupResult result =
                new BudgetGroupResult(
                        groupId,
                        budgetId,
                        null,
                        "Fundação",
                        0
                );

        when(
                createBudgetGroupUseCase.execute(
                        any(CreateBudgetGroupCommand.class)
                )
        ).thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/{budgetId}/groups",
                                budgetId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "parentGroupId": null,
                                      "name": "Fundação",
                                      "sortOrder": 0
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(groupId.toString())
                )
                .andExpect(
                        jsonPath("$.budgetId")
                                .value(budgetId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Fundação")
                )
                .andExpect(
                        jsonPath("$.sortOrder")
                                .value(0)
                );

        ArgumentCaptor<CreateBudgetGroupCommand> captor =
                ArgumentCaptor.forClass(
                        CreateBudgetGroupCommand.class
                );

        verify(
                createBudgetGroupUseCase
        ).execute(captor.capture());

        CreateBudgetGroupCommand command =
                captor.getValue();

        assertEquals(
                budgetId,
                command.budgetId()
        );

        assertEquals(
                "Fundação",
                command.name()
        );

        assertEquals(
                0,
                command.sortOrder()
        );
    }

    @Test
    void shouldCreateChildBudgetGroup() throws Exception {

        UUID budgetId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID parentGroupId = UUID.randomUUID();

        BudgetGroupResult result =
                new BudgetGroupResult(
                        groupId,
                        budgetId,
                        parentGroupId,
                        "Escavação",
                        1
                );

        when(
                createBudgetGroupUseCase.execute(
                        any(CreateBudgetGroupCommand.class)
                )
        ).thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/{budgetId}/groups",
                                budgetId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "parentGroupId": "%s",
                                      "name": "Escavação",
                                      "sortOrder": 1
                                    }
                                    """.formatted(
                                        parentGroupId
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.parentGroupId")
                                .value(
                                        parentGroupId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Escavação")
                );
    }

    @Test
    void shouldReturnBadRequestWhenCreateGroupRequestIsInvalid()
            throws Exception {

        UUID budgetId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/{budgetId}/groups",
                                budgetId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "parentGroupId": null,
                                      "name": "",
                                      "sortOrder": -1
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                createBudgetGroupUseCase,
                never()
        ).execute(any());
    }

    @Test
    void shouldReturnNotFoundWhenCreatingGroupForMissingBudget()
            throws Exception {

        UUID budgetId = UUID.randomUUID();

        when(
                createBudgetGroupUseCase.execute(
                        any(CreateBudgetGroupCommand.class)
                )
        ).thenThrow(
                new BudgetNotFoundException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/{budgetId}/groups",
                                budgetId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "parentGroupId": null,
                                      "name": "Fundação",
                                      "sortOrder": 0
                                    }
                                    """)
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Orçamento não encontrado")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/budgets/"
                                                + budgetId
                                                + "/groups"
                                )
                );
    }

    @Test
    void shouldReturnBadRequestWhenParentGroupIsInvalid()
            throws Exception {

        UUID budgetId = UUID.randomUUID();
        UUID parentGroupId = UUID.randomUUID();

        when(
                createBudgetGroupUseCase.execute(
                        any(CreateBudgetGroupCommand.class)
                )
        ).thenThrow(
                new InvalidParentBudgetGroupException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/{budgetId}/groups",
                                budgetId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "parentGroupId": "%s",
                                      "name": "Escavação",
                                      "sortOrder": 0
                                    }
                                    """.formatted(
                                        parentGroupId
                                ))
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "O grupo de orçamento pai é inválido."
                                )
                );
    }
}