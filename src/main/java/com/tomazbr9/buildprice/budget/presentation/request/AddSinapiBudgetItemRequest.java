package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AddSinapiBudgetItemRequest(

        @NotNull(message = "Composition id is required")
        UUID compositionId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(
                value = "0.0001",
                message = "Quantity must be greater than zero"
        )
        BigDecimal quantity

) {
}