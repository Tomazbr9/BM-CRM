package com.tomazbr9.buildprice.budget.presentation.controller;

import com.tomazbr9.buildprice.budget.application.command.*;
import com.tomazbr9.buildprice.budget.application.dto.*;
import com.tomazbr9.buildprice.budget.application.exception.*;
import com.tomazbr9.buildprice.budget.application.port.in.*;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;
import com.tomazbr9.buildprice.budget.domain.enums.BudgetStatus;
import com.tomazbr9.buildprice.budget.domain.exception.InvalidBudgetStatusTransitionException;
import com.tomazbr9.buildprice.budget.presentation.exception.BudgetExceptionHandler;
import com.tomazbr9.buildprice.identity.infrastructure.security.JwtAuthenticationFilter;
import com.tomazbr9.buildprice.identity.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private UpdateBudgetBdiUseCase updateBudgetBdiUseCase;

    @MockitoBean
    private CompleteBudgetUseCase completeBudgetUseCase;

    @MockitoBean
    private ReopenBudgetUseCase reopenBudgetUseCase;

    @MockitoBean
    private DeleteBudgetItemUseCase deleteBudgetItemUseCase;

    @MockitoBean
    private UpdateBudgetItemQuantityUseCase updateBudgetItemQuantityUseCase;

    @MockitoBean
    private AddManualBudgetItemUseCase addManualBudgetItemUseCase;

    @MockitoBean
    private CreateBudgetGroupUseCase createBudgetGroupUseCase;

    @MockitoBean
    private AddSinapiBudgetItemUseCase addSinapiBudgetItemUseCase;

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

    @Test
    void shouldAddSinapiItemToBudgetGroup() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        BudgetItemResult result =
                new BudgetItemResult(
                        itemId,
                        groupId,
                        BudgetItemType.SINAPI,
                        compositionId,
                        null,
                        new BigDecimal("2.50"),
                        new BigDecimal("100.00"),
                        new BigDecimal("250.00")
                );

        when(
                addSinapiBudgetItemUseCase.execute(
                        any(AddSinapiBudgetItemCommand.class)
                )
        ).thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/sinapi",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "compositionId": "%s",
                                      "quantity": 2.50
                                    }
                                    """.formatted(
                                        compositionId
                                ))
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(itemId.toString())
                )
                .andExpect(
                        jsonPath("$.budgetGroupId")
                                .value(groupId.toString())
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("SINAPI")
                )
                .andExpect(
                        jsonPath("$.compositionId")
                                .value(compositionId.toString())
                )
                .andExpect(
                        jsonPath("$.manualItemId")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(2.50)
                )
                .andExpect(
                        jsonPath("$.unitPriceSnapshot")
                                .value(100.00)
                )
                .andExpect(
                        jsonPath("$.directCost")
                                .value(250.00)
                );

        ArgumentCaptor<AddSinapiBudgetItemCommand> captor =
                ArgumentCaptor.forClass(
                        AddSinapiBudgetItemCommand.class
                );

        verify(
                addSinapiBudgetItemUseCase
        ).execute(captor.capture());

        AddSinapiBudgetItemCommand command =
                captor.getValue();

        assertEquals(
                groupId,
                command.budgetGroupId()
        );

        assertEquals(
                compositionId,
                command.compositionId()
        );

        assertEquals(
                0,
                new BigDecimal("2.50")
                        .compareTo(
                                command.quantity()
                        )
        );
    }

    @Test
    void shouldReturnBadRequestWhenSinapiItemRequestIsInvalid()
            throws Exception {

        UUID groupId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/sinapi",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "compositionId": null,
                                      "quantity": 0
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                addSinapiBudgetItemUseCase,
                never()
        ).execute(any());
    }

    @Test
    void shouldReturnNotFoundWhenBudgetGroupDoesNotExistForSinapiItem()
            throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        when(
                addSinapiBudgetItemUseCase.execute(
                        any(AddSinapiBudgetItemCommand.class)
                )
        ).thenThrow(
                new BudgetGroupNotFoundException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/sinapi",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "compositionId": "%s",
                                      "quantity": 1
                                    }
                                    """.formatted(
                                        compositionId
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
                                .value("Grupo de orçamento não encontrado")
                );
    }

    @Test
    void shouldReturnBadRequestWhenCompositionIsNotAvailableForBudget()
            throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        when(
                addSinapiBudgetItemUseCase.execute(
                        any(AddSinapiBudgetItemCommand.class)
                )
        ).thenThrow(
                new CompositionNotAvailableForBudgetException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/sinapi",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "compositionId": "%s",
                                      "quantity": 1
                                    }
                                    """.formatted(
                                        compositionId
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
                                        "A composição não está disponível para este orçamento."
                                )
                );
    }

    @Test
    void shouldAddManualItemToBudgetGroup() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID manualItemId = UUID.randomUUID();

        BudgetItemResult result =
                new BudgetItemResult(
                        itemId,
                        groupId,
                        BudgetItemType.MANUAL,
                        null,
                        manualItemId,
                        new BigDecimal("2.00"),
                        new BigDecimal("150.00"),
                        new BigDecimal("300.00")
                );

        when(
                addManualBudgetItemUseCase.execute(
                        any(AddManualBudgetItemCommand.class)
                )
        ).thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/manual",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "description": "Transporte especial de material",
                                      "unit": "UN",
                                      "unitPrice": 150.00,
                                      "quantity": 2
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(itemId.toString())
                )
                .andExpect(
                        jsonPath("$.budgetGroupId")
                                .value(groupId.toString())
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("MANUAL")
                )
                .andExpect(
                        jsonPath("$.compositionId")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$.manualItemId")
                                .value(manualItemId.toString())
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(2.00)
                )
                .andExpect(
                        jsonPath("$.unitPriceSnapshot")
                                .value(150.00)
                )
                .andExpect(
                        jsonPath("$.directCost")
                                .value(300.00)
                );

        ArgumentCaptor<AddManualBudgetItemCommand> captor =
                ArgumentCaptor.forClass(
                        AddManualBudgetItemCommand.class
                );

        verify(
                addManualBudgetItemUseCase
        ).execute(captor.capture());

        AddManualBudgetItemCommand command =
                captor.getValue();

        assertEquals(
                groupId,
                command.budgetGroupId()
        );

        assertEquals(
                "Transporte especial de material",
                command.description()
        );

        assertEquals(
                "UN",
                command.unit()
        );

        assertEquals(
                0,
                new BigDecimal("150.00")
                        .compareTo(
                                command.unitPrice()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("2.00")
                        .compareTo(
                                command.quantity()
                        )
        );
    }

    @Test
    void shouldReturnBadRequestWhenManualItemRequestIsInvalid()
            throws Exception {

        UUID groupId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/manual",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "description": "",
                                      "unit": "",
                                      "unitPrice": -1,
                                      "quantity": 0
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                addManualBudgetItemUseCase,
                never()
        ).execute(any());
    }

    @Test
    void shouldReturnNotFoundWhenBudgetGroupDoesNotExistForManualItem()
            throws Exception {

        UUID groupId = UUID.randomUUID();

        when(
                addManualBudgetItemUseCase.execute(
                        any(AddManualBudgetItemCommand.class)
                )
        ).thenThrow(
                new BudgetGroupNotFoundException()
        );

        mockMvc.perform(
                        post(
                                "/api/v1/budgets/groups/{groupId}/items/manual",
                                groupId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "description": "Transporte especial",
                                      "unit": "UN",
                                      "unitPrice": 150.00,
                                      "quantity": 2
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
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Grupo de orçamento não encontrado")
                );
    }

    @Test
    void shouldUpdateBudgetItemQuantity() throws Exception {

        UUID itemId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();

        BudgetItemResult result = new BudgetItemResult(
                itemId,
                groupId,
                BudgetItemType.SINAPI,
                compositionId,
                null,
                new BigDecimal("5.50"),
                new BigDecimal("100.00"),
                new BigDecimal("550.00")
        );

        when(updateBudgetItemQuantityUseCase.execute(
                any(UpdateBudgetItemQuantityCommand.class)
        )).thenReturn(result);

        mockMvc.perform(
                        patch("/api/v1/budgets/items/{itemId}/quantity", itemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantity": 5.50
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.budgetGroupId").value(groupId.toString()))
                .andExpect(jsonPath("$.type").value("SINAPI"))
                .andExpect(jsonPath("$.quantity").value(5.50))
                .andExpect(jsonPath("$.unitPriceSnapshot").value(100.00))
                .andExpect(jsonPath("$.directCost").value(550.00));

        ArgumentCaptor<UpdateBudgetItemQuantityCommand> captor =
                ArgumentCaptor.forClass(UpdateBudgetItemQuantityCommand.class);

        verify(updateBudgetItemQuantityUseCase).execute(captor.capture());

        UpdateBudgetItemQuantityCommand command = captor.getValue();

        assertEquals(itemId, command.itemId());

        assertEquals(
                0,
                new BigDecimal("5.50").compareTo(command.quantity())
        );
    }

    @Test
    void shouldReturnBadRequestWhenBudgetItemQuantityIsInvalid()
            throws Exception {

        UUID itemId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/budgets/items/{itemId}/quantity", itemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantity": 0
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verify(
                updateBudgetItemQuantityUseCase,
                never()
        ).execute(any());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingBudgetItem()
            throws Exception {

        UUID itemId = UUID.randomUUID();

        when(updateBudgetItemQuantityUseCase.execute(
                any(UpdateBudgetItemQuantityCommand.class)
        )).thenThrow(new BudgetItemNotFoundException());

        mockMvc.perform(
                        patch("/api/v1/budgets/items/{itemId}/quantity", itemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantity": 3
                                    }
                                    """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Orçamento não encontrado"))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/budgets/items/" + itemId + "/quantity"
                ));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingItemFromInaccessibleBudget()
            throws Exception {

        UUID itemId = UUID.randomUUID();

        when(updateBudgetItemQuantityUseCase.execute(
                any(UpdateBudgetItemQuantityCommand.class)
        )).thenThrow(new BudgetNotFoundException());

        mockMvc.perform(
                        patch("/api/v1/budgets/items/{itemId}/quantity", itemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "quantity": 3
                                    }
                                    """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Orçamento não encontrado"));
    }

    @Test
    void shouldDeleteBudgetItem() throws Exception {

        UUID itemId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/v1/budgets/items/{itemId}", itemId)
                                .with(csrf())
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteBudgetItemUseCase)
                .execute(itemId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingBudgetItem()
            throws Exception {

        UUID itemId = UUID.randomUUID();

        doThrow(new BudgetItemNotFoundException())
                .when(deleteBudgetItemUseCase)
                .execute(itemId);

        mockMvc.perform(
                        delete("/api/v1/budgets/items/{itemId}", itemId)
                                .with(csrf())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Orçamento não encontrado"))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/budgets/items/" + itemId
                ));
    }

    @Test
    void shouldUpdateBudgetBdi() throws Exception {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        BudgetResult result = new BudgetResult(
                budgetId,
                projectId,
                versionId,
                BudgetStatus.DRAFT,
                new BigDecimal("25.50")
        );

        when(updateBudgetBdiUseCase.execute(
                any(UpdateBudgetBdiCommand.class)
        )).thenReturn(result);

        mockMvc.perform(
                        patch("/api/v1/budgets/{budgetId}/bdi", budgetId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "bdiPercentage": 25.50
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.sinapiTableVersionId").value(versionId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.bdiPercentage").value(25.50));

        ArgumentCaptor<UpdateBudgetBdiCommand> captor =
                ArgumentCaptor.forClass(UpdateBudgetBdiCommand.class);

        verify(updateBudgetBdiUseCase).execute(captor.capture());

        UpdateBudgetBdiCommand command = captor.getValue();

        assertEquals(budgetId, command.budgetId());

        assertEquals(
                0,
                new BigDecimal("25.50").compareTo(command.bdiPercentage())
        );
    }

    @Test
    void shouldAllowZeroBdi() throws Exception {

        UUID budgetId = UUID.randomUUID();

        BudgetResult result = new BudgetResult(
                budgetId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BudgetStatus.DRAFT,
                BigDecimal.ZERO
        );

        when(updateBudgetBdiUseCase.execute(
                any(UpdateBudgetBdiCommand.class)
        )).thenReturn(result);

        mockMvc.perform(
                        patch("/api/v1/budgets/{budgetId}/bdi", budgetId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "bdiPercentage": 0
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bdiPercentage").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"bdiPercentage\": -1}",
            "{\"bdiPercentage\": null}",
            "{\"bdiPercentage\": 10.12345}"
    })
    void shouldReturnBadRequestWhenBdiIsInvalid(String body)
            throws Exception {

        UUID budgetId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/budgets/{budgetId}/bdi", budgetId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updateBudgetBdiUseCase);
    }

    @Test
    void shouldCompleteBudget() throws Exception {

        UUID budgetId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        BudgetResult result = new BudgetResult(
                budgetId,
                projectId,
                versionId,
                BudgetStatus.COMPLETED,
                new BigDecimal("25.50")
        );

        when(completeBudgetUseCase.execute(budgetId))
                .thenReturn(result);

        mockMvc.perform(
                        patch("/api/v1/budgets/{budgetId}/complete", budgetId)
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.sinapiTableVersionId").value(versionId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.bdiPercentage").value(25.50));

        verify(completeBudgetUseCase).execute(budgetId);
        verifyNoInteractions(reopenBudgetUseCase);
    }

    @Test
    void shouldReopenBudget() throws Exception {

        UUID budgetId = UUID.randomUUID();

        BudgetResult result = new BudgetResult(
                budgetId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BudgetStatus.DRAFT,
                new BigDecimal("25.50")
        );

        when(reopenBudgetUseCase.execute(budgetId))
                .thenReturn(result);

        mockMvc.perform(
                        patch("/api/v1/budgets/{budgetId}/reopen", budgetId)
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.bdiPercentage").value(25.50));

        verify(reopenBudgetUseCase).execute(budgetId);
        verifyNoInteractions(completeBudgetUseCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"complete", "reopen"})
    void shouldReturnConflictForInvalidBudgetStatusTransition(
            String action
    ) throws Exception {

        UUID budgetId = UUID.randomUUID();

        String message;

        if (action.equals("complete")) {
            message = "Only draft budgets can be completed";

            when(completeBudgetUseCase.execute(budgetId))
                    .thenThrow(
                            new InvalidBudgetStatusTransitionException(message)
                    );
        } else {
            message = "Only completed budgets can be reopened";

            when(reopenBudgetUseCase.execute(budgetId))
                    .thenThrow(
                            new InvalidBudgetStatusTransitionException(message)
                    );
        }

        mockMvc.perform(
                        patch("/api/v1/budgets/{budgetId}/{action}",
                                budgetId, action)
                                .with(csrf())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/budgets/" + budgetId + "/" + action
                ));
    }


}