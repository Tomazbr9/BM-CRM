package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddManualBudgetItemRequest(

        @NotBlank(message = "Description is required")
        @Size(
                max = 255,
                message = "Description must have at most 255 characters"
        )
        String description,

        @NotBlank(message = "Unit is required")
        @Size(
                max = 50,
                message = "Unit must have at most 50 characters"
        )
        String unit,

        @NotNull(message = "Unit price is required")
        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Unit price cannot be negative"
        )
        BigDecimal unitPrice,

        @NotNull(message = "Quantity is required")
        @DecimalMin(
                value = "0.0001",
                message = "Quantity must be greater than zero"
        )
        BigDecimal quantity

) {
}