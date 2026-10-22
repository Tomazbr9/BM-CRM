
package com.tomazbr9.buildprice.reports.presentation.controller;

import com.tomazbr9.buildprice.reports.application.dto.AbcCurveResult;
import com.tomazbr9.buildprice.reports.application.port.in.ExportBudgetPdfUseCase;
import com.tomazbr9.buildprice.reports.application.port.in.GetBudgetAbcCurveUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final GetBudgetAbcCurveUseCase getBudgetAbcCurveUseCase;
    private final ExportBudgetPdfUseCase exportBudgetPdfUseCase;

    @GetMapping("/budgets/{budgetId}/abc-curve")
    public ResponseEntity<AbcCurveResult> getBudgetAbcCurve(
            @PathVariable UUID budgetId
    ) {
        AbcCurveResult result =
                getBudgetAbcCurveUseCase.execute(budgetId);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/budgets/{budgetId}/export/pdf")
    public ResponseEntity<byte[]> exportBudgetPdf(
            @PathVariable UUID budgetId
    ) {

        byte[] pdf = exportBudgetPdfUseCase.execute(budgetId);

        String filename = "orcamento-" + budgetId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .contentLength(pdf.length)
                .body(pdf);
    }

}
