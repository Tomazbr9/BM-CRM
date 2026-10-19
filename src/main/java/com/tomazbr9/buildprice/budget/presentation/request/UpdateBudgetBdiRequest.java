package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record UpdateBudgetBdiRequest(

        @NotNull(message = "BDI percentage is required")
        @DecimalMin(
                value = "0.0",
                message = "BDI percentage cannot be negative"
        )
        @Digits(
                integer = 6,
                fraction = 4,
                message = "BDI percentage must have at most 6 integer and 4 decimal digits"
        )
        BigDecimal bdiPercentage

) {
}