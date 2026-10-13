package com.tomazbr9.buildprice.budget.presentation.controller;

import com.tomazbr9.buildprice.budget.application.command.CreateBudgetCommand;
import com.tomazbr9.buildprice.budget.application.command.CreateBudgetGroupCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetGroupResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.in.CreateBudgetGroupUseCase;
import com.tomazbr9.buildprice.budget.application.port.in.CreateBudgetUseCase;
import com.tomazbr9.buildprice.budget.application.port.in.GetBudgetStructureUseCase;
import com.tomazbr9.buildprice.budget.presentation.request.CreateBudgetGroupRequest;
import com.tomazbr9.buildprice.budget.presentation.request.CreateBudgetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final GetBudgetStructureUseCase getBudgetStructureUseCase;
    private final CreateBudgetUseCase createBudgetUseCase;
    private final CreateBudgetGroupUseCase createBudgetGroupUseCase;

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetStructureResult> getBudgetStructure(
            @PathVariable UUID budgetId
    ) {

        BudgetStructureResult result =
                getBudgetStructureUseCase.execute(
                        budgetId
                );

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<BudgetResult> create(
            @Valid @RequestBody CreateBudgetRequest request
    ) {

        CreateBudgetCommand command =
                new CreateBudgetCommand(
                        request.projectId(),
                        request.sinapiTableVersionId()
                );

        BudgetResult result =
                createBudgetUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @PostMapping("/{budgetId}/groups")
    public ResponseEntity<BudgetGroupResult> createGroup(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateBudgetGroupRequest request
    ) {

        CreateBudgetGroupCommand command =
                new CreateBudgetGroupCommand(
                        budgetId,
                        request.parentGroupId(),
                        request.name(),
                        request.sortOrder()
                );

        BudgetGroupResult result =
                createBudgetGroupUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}