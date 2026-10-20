
package com.tomazbr9.buildprice.reports.presentation.controller;

import com.tomazbr9.buildprice.reports.application.dto.AbcCurveResult;
import com.tomazbr9.buildprice.reports.application.port.in.GetBudgetAbcCurveUseCase;

import lombok.RequiredArgsConstructor;
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

    @GetMapping("/budgets/{budgetId}/abc-curve")
    public ResponseEntity<AbcCurveResult> getBudgetAbcCurve(
            @PathVariable UUID budgetId
    ) {
        AbcCurveResult result =
                getBudgetAbcCurveUseCase.execute(budgetId);

        return ResponseEntity.ok(result);
    }
}
