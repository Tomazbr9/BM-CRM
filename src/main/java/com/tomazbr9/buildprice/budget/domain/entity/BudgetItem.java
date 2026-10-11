package com.tomazbr9.buildprice.budget.domain.entity;

import com.tomazbr9.buildprice.budget.domain.enums.BudgetItemType;

import java.math.BigDecimal;
import java.util.UUID;

public class BudgetItem {

    private final UUID id;
    private final UUID budgetGroupId;

    private final BudgetItemType type;

    private final UUID compositionId;
    private final UUID manualItemId;

    private BigDecimal quantity;

    private final BigDecimal unitPriceSnapshot;

    private BudgetItem(
            UUID id,
            UUID budgetGroupId,
            BudgetItemType type,
            UUID compositionId,
            UUID manualItemId,
            BigDecimal quantity,
            BigDecimal unitPriceSnapshot
    ) {
        this.id = validateId(id);
        this.budgetGroupId =
                validateBudgetGroupId(
                        budgetGroupId
                );

        this.type = validateType(type);

        validateReference(
                type,
                compositionId,
                manualItemId
        );

        this.compositionId = compositionId;
        this.manualItemId = manualItemId;

        this.quantity =
                validateQuantity(quantity);

        this.unitPriceSnapshot =
                validateUnitPriceSnapshot(
                        unitPriceSnapshot
                );
    }

    public static BudgetItem createSinapi(
            UUID budgetGroupId,
            UUID compositionId,
            BigDecimal quantity,
            BigDecimal unitPriceSnapshot
    ) {
        return new BudgetItem(
                UUID.randomUUID(),
                budgetGroupId,
                BudgetItemType.SINAPI,
                compositionId,
                null,
                quantity,
                unitPriceSnapshot
        );
    }

    public static BudgetItem createManual(
            UUID budgetGroupId,
            UUID manualItemId,
            BigDecimal quantity,
            BigDecimal unitPriceSnapshot
    ) {
        return new BudgetItem(
                UUID.randomUUID(),
                budgetGroupId,
                BudgetItemType.MANUAL,
                null,
                manualItemId,
                quantity,
                unitPriceSnapshot
        );
    }

    public static BudgetItem restore(
            UUID id,
            UUID budgetGroupId,
            BudgetItemType type,
            UUID compositionId,
            UUID manualItemId,
            BigDecimal quantity,
            BigDecimal unitPriceSnapshot
    ) {
        return new BudgetItem(
                id,
                budgetGroupId,
                type,
                compositionId,
                manualItemId,
                quantity,
                unitPriceSnapshot
        );
    }

    public void updateQuantity(
            BigDecimal quantity
    ) {
        this.quantity =
                validateQuantity(quantity);
    }

    public BigDecimal calculateDirectCost() {
        return unitPriceSnapshot
                .multiply(quantity);
    }

    private static UUID validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Budget item id is required"
            );
        }

        return id;
    }

    private static UUID validateBudgetGroupId(
            UUID budgetGroupId
    ) {
        if (budgetGroupId == null) {
            throw new IllegalArgumentException(
                    "Budget group id is required"
            );
        }

        return budgetGroupId;
    }

    private static BudgetItemType validateType(
            BudgetItemType type
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Budget item type is required"
            );
        }

        return type;
    }

    private static void validateReference(
            BudgetItemType type,
            UUID compositionId,
            UUID manualItemId
    ) {

        if (type == BudgetItemType.SINAPI) {

            if (compositionId == null
                    || manualItemId != null) {

                throw new IllegalArgumentException(
                        "SINAPI item must reference only a composition"
                );
            }

            return;
        }

        if (type == BudgetItemType.MANUAL) {

            if (manualItemId == null
                    || compositionId != null) {

                throw new IllegalArgumentException(
                        "Manual item must reference only a manual item"
                );
            }
        }
    }

    private static BigDecimal validateQuantity(
            BigDecimal quantity
    ) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quantity is required"
            );
        }

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        return quantity;
    }

    private static BigDecimal validateUnitPriceSnapshot(
            BigDecimal unitPriceSnapshot
    ) {
        if (unitPriceSnapshot == null) {
            throw new IllegalArgumentException(
                    "Unit price snapshot is required"
            );
        }

        if (unitPriceSnapshot.signum() < 0) {
            throw new IllegalArgumentException(
                    "Unit price snapshot cannot be negative"
            );
        }

        return unitPriceSnapshot;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBudgetGroupId() {
        return budgetGroupId;
    }

    public BudgetItemType getType() {
        return type;
    }

    public UUID getCompositionId() {
        return compositionId;
    }

    public UUID getManualItemId() {
        return manualItemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }
}