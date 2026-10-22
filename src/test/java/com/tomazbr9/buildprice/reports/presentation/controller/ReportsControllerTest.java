
package com.tomazbr9.buildprice.reports.presentation.controller;

import com.tomazbr9.buildprice.budget.application.exception.BudgetNotFoundException;
import com.tomazbr9.buildprice.budget.presentation.exception.BudgetExceptionHandler;
import com.tomazbr9.buildprice.identity.infrastructure.security.JwtService;
import com.tomazbr9.buildprice.reports.application.dto.AbcCurveItemResult;
import com.tomazbr9.buildprice.reports.application.dto.AbcCurveResult;
import com.tomazbr9.buildprice.reports.application.port.in.ExportBudgetPdfUseCase;
import com.tomazbr9.buildprice.reports.application.port.in.GetBudgetAbcCurveUseCase;
import com.tomazbr9.buildprice.reports.domain.enums.AbcClassification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportsController.class)
@Import(BudgetExceptionHandler.class)
@WithMockUser(username = "test-user", roles = "USER")
class ReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ExportBudgetPdfUseCase exportBudgetPdfUseCase;

    @MockitoBean
    private GetBudgetAbcCurveUseCase getBudgetAbcCurveUseCase;

    @Test
    void shouldReturnAbcCurveWithClassifications() throws Exception {

        UUID budgetId = UUID.randomUUID();

        AbcCurveResult result = new AbcCurveResult(
                budgetId,
                new BigDecimal("1000.00"),
                List.of(
                        item("Estrutura", "800.00", "80.00",
                                "80.00", AbcClassification.A),
                        item("Pintura", "150.00", "15.00",
                                "95.00", AbcClassification.B),
                        item("Limpeza", "50.00", "5.00",
                                "100.00", AbcClassification.C)
                )
        );

        when(getBudgetAbcCurveUseCase.execute(budgetId))
                .thenReturn(result);

        mockMvc.perform(
                        get("/api/v1/reports/budgets/{budgetId}/abc-curve",
                                budgetId)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/json"))
                .andExpect(jsonPath("$.budgetId")
                        .value(budgetId.toString()))
                .andExpect(jsonPath("$.totalDirectCost")
                        .value(1000.00))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].description")
                        .value("Estrutura"))
                .andExpect(jsonPath("$.items[0].classification")
                        .value("A"))
                .andExpect(jsonPath("$.items[0].accumulatedPercentage")
                        .value(80.00))
                .andExpect(jsonPath("$.items[1].classification")
                        .value("B"))
                .andExpect(jsonPath("$.items[2].classification")
                        .value("C"))
                .andExpect(jsonPath("$.items[2].accumulatedPercentage")
                        .value(100.00));

        verify(getBudgetAbcCurveUseCase).execute(budgetId);
    }

    @Test
    void shouldReturnEmptyAbcCurve() throws Exception {

        UUID budgetId = UUID.randomUUID();

        when(getBudgetAbcCurveUseCase.execute(budgetId))
                .thenReturn(new AbcCurveResult(
                        budgetId,
                        new BigDecimal("0.00"),
                        List.of()
                ));

        mockMvc.perform(
                        get("/api/v1/reports/budgets/{budgetId}/abc-curve",
                                budgetId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId")
                        .value(budgetId.toString()))
                .andExpect(jsonPath("$.totalDirectCost")
                        .value(0.00))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(getBudgetAbcCurveUseCase).execute(budgetId);
    }

    @Test
    void shouldReturnBadRequestForInvalidBudgetId() throws Exception {

        mockMvc.perform(
                        get("/api/v1/reports/budgets/{budgetId}/abc-curve",
                                "invalid-uuid")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getBudgetAbcCurveUseCase);
    }

    private AbcCurveItemResult item(
            String description,
            String directCost,
            String participation,
            String accumulated,
            AbcClassification classification
    ) {
        BigDecimal cost = new BigDecimal(directCost);

        return new AbcCurveItemResult(
                UUID.randomUUID(),
                "MANUAL",
                null,
                description,
                "UN",
                BigDecimal.ONE,
                cost,
                cost,
                new BigDecimal(participation),
                new BigDecimal(accumulated),
                classification
        );
    }


    @Test
    void shouldReturnNotFoundWhenBudgetDoesNotExist()
            throws Exception {

        UUID budgetId = UUID.randomUUID();

        when(getBudgetAbcCurveUseCase.execute(budgetId))
                .thenThrow(new BudgetNotFoundException());

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/abc-curve",
                                budgetId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Orçamento não encontrado"));

        verify(getBudgetAbcCurveUseCase).execute(budgetId);
    }


    @Test
    @WithAnonymousUser
    void shouldRejectUnauthenticatedRequest() throws Exception {

        UUID budgetId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/abc-curve",
                                budgetId
                        )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(getBudgetAbcCurveUseCase);
    }


    @Test
    void shouldExportBudgetPdf() throws Exception {

        UUID budgetId = UUID.randomUUID();

        byte[] pdf = "%PDF-1.7\nmock-pdf"
                .getBytes(StandardCharsets.US_ASCII);

        when(exportBudgetPdfUseCase.execute(budgetId))
                .thenReturn(pdf);

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/export/pdf",
                                budgetId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_PDF
                ))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"orcamento-"
                                + budgetId + ".pdf\""
                ))
                .andExpect(header().longValue(
                        HttpHeaders.CONTENT_LENGTH,
                        pdf.length
                ))
                .andExpect(content().bytes(pdf));

        verify(exportBudgetPdfUseCase).execute(budgetId);
    }


    @Test
    @WithAnonymousUser
    void shouldRejectUnauthenticatedPdfExport() throws Exception {

        UUID budgetId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/reports/budgets/{budgetId}/export/pdf",
                                budgetId
                        )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(exportBudgetPdfUseCase);
    }


}
