package com.tomazbr9.buildprice.budget.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class ManualItem {

    private final UUID id;
    private final UUID budgetId;

    private String description;
    private String unit;
    private BigDecimal unitPrice;

    private ManualItem(
            UUID id,
            UUID budgetId,
            String description,
            String unit,
            BigDecimal unitPrice
    ) {
        this.id = validateId(id);
        this.budgetId = validateBudgetId(budgetId);
        this.description = validateDescription(description);
        this.unit = validateUnit(unit);
        this.unitPrice = validateUnitPrice(unitPrice);
    }

    public static ManualItem create(
            UUID budgetId,
            String description,
            String unit,
            BigDecimal unitPrice
    ) {
        return new ManualItem(
                UUID.randomUUID(),
                budgetId,
                description,
                unit,
                unitPrice
        );
    }

    public static ManualItem restore(
            UUID id,
            UUID budgetId,
            String description,
            String unit,
            BigDecimal unitPrice
    ) {
        return new ManualItem(
                id,
                budgetId,
                description,
                unit,
                unitPrice
        );
    }

    public void update(
            String description,
            String unit,
            BigDecimal unitPrice
    ) {
        this.description = validateDescription(description);
        this.unit = validateUnit(unit);
        this.unitPrice = validateUnitPrice(unitPrice);
    }

    private static UUID validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID do item manual é obrigatório"
            );
        }

        return id;
    }

    private static UUID validateBudgetId(UUID budgetId) {
        if (budgetId == null) {
            throw new IllegalArgumentException(
                    "O ID do orçamento é obrigatório"
            );
        }

        return budgetId;
    }

    private static String validateDescription(
            String description
    ) {
        if (description == null
                || description.isBlank()) {

            throw new IllegalArgumentException(
                    "A descrição é obrigatória"
            );
        }

        return description.trim();
    }

    private static String validateUnit(
            String unit
    ) {
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException(
                    "A unidade é obrigatória"
            );
        }

        return unit.trim();
    }

    private static BigDecimal validateUnitPrice(
            BigDecimal unitPrice
    ) {
        if (unitPrice == null) {
            throw new IllegalArgumentException(
                    "O preço unitário é obrigatório"
            );
        }

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "O preço unitário não pode ser negativo"
            );
        }

        return unitPrice;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBudgetId() {
        return budgetId;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}