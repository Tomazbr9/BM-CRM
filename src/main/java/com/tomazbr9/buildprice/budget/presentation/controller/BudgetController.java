package com.tomazbr9.buildprice.budget.presentation.controller;

import com.tomazbr9.buildprice.budget.application.command.*;
import com.tomazbr9.buildprice.budget.application.dto.BudgetGroupResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetItemResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetResult;
import com.tomazbr9.buildprice.budget.application.dto.BudgetStructureResult;
import com.tomazbr9.buildprice.budget.application.port.in.*;
import com.tomazbr9.buildprice.budget.presentation.request.*;
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
    private final UpdateBudgetItemQuantityUseCase updateBudgetItemQuantityUseCase;
    private final DeleteBudgetItemUseCase deleteBudgetItemUseCase;
    private final UpdateBudgetBdiUseCase updateBudgetBdiUseCase;
    private final CompleteBudgetUseCase completeBudgetUseCase;
    private final ReopenBudgetUseCase reopenBudgetUseCase;

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

    @PatchMapping("/items/{itemId}/quantity")
    public ResponseEntity<BudgetItemResult> updateItemQuantity(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateBudgetItemQuantityRequest request
    ) {
        UpdateBudgetItemQuantityCommand command =
                new UpdateBudgetItemQuantityCommand(
                        itemId,
                        request.quantity()
                );

        BudgetItemResult result =
                updateBudgetItemQuantityUseCase.execute(command);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID itemId
    ) {
        deleteBudgetItemUseCase.execute(itemId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{budgetId}/bdi")
    public ResponseEntity<BudgetResult> updateBdi(
            @PathVariable UUID budgetId,
            @Valid @RequestBody UpdateBudgetBdiRequest request
    ) {
        UpdateBudgetBdiCommand command =
                new UpdateBudgetBdiCommand(
                        budgetId,
                        request.bdiPercentage()
                );

        BudgetResult result =
                updateBudgetBdiUseCase.execute(command);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{budgetId}/complete")
    public ResponseEntity<BudgetResult> completeBudget(
            @PathVariable UUID budgetId
    ) {
        BudgetResult result = completeBudgetUseCase.execute(budgetId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{budgetId}/reopen")
    public ResponseEntity<BudgetResult> reopenBudget(
            @PathVariable UUID budgetId
    ) {
        BudgetResult result = reopenBudgetUseCase.execute(budgetId);

        return ResponseEntity.ok(result);
    }
}