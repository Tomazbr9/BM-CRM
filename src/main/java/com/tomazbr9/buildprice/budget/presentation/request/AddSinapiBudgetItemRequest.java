package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AddSinapiBudgetItemRequest(

        @NotNull(message = "O ID da composição é obrigatório")
        UUID compositionId,

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(
                value = "0.0001",
                message = "A quantidade deve ser maior que zero"
        )
        BigDecimal quantity

) {
}