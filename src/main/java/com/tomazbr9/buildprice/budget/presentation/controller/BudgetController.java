package com.tomazbr9.buildprice.budget.presentation.controller;

import com.tomazbr9.buildprice.budget.application.command.AddManualBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.command.AddSinapiBudgetItemCommand;
import com.tomazbr9.buildprice.budget.application.command.CreateBudgetCommand;
import com.tomazbr9.buildprice.budget.application.command.CreateBudgetGroupCommand;
import com.tomazbr9.buildprice.budget.application.dto.BudgetGroupResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.in.*;
import com.tomazbr9.buildprice.budget.presentation.request.AddManualBudgetItemRequest;
import com.tomazbr9.buildprice.budget.presentation.request.AddSinapiBudgetItemRequest;
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
    private final AddSinapiBudgetItemUseCase addSinapiBudgetItemUseCase;
    private final AddManualBudgetItemUseCase addManualBudgetItemUseCase;

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

    @PostMapping("/groups/{groupId}/items/sinapi")
    public ResponseEntity<BudgetItemResult> addSinapiItem(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddSinapiBudgetItemRequest request
    ) {

        AddSinapiBudgetItemCommand command =
                new AddSinapiBudgetItemCommand(
                        groupId,
                        request.compositionId(),
                        request.quantity()
                );

        BudgetItemResult result =
                addSinapiBudgetItemUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @PostMapping("/groups/{groupId}/items/manual")
    public ResponseEntity<BudgetItemResult> addManualItem(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddManualBudgetItemRequest request
    ) {

        AddManualBudgetItemCommand command =
                new AddManualBudgetItemCommand(
                        groupId,
                        request.description(),
                        request.unit(),
                        request.unitPrice(),
                        request.quantity()
                );

        BudgetItemResult result =
                addManualBudgetItemUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}