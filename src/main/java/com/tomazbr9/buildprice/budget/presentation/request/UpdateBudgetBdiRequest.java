package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record UpdateBudgetBdiRequest(

        @NotNull(message = "O percentual de BDI é obrigatório")
        @DecimalMin(
                value = "0.0",
                message = "O percentual de BDI não pode ser negativo"
        )
        @Digits(
                integer = 6,
                fraction = 4,
                message = "O percentual de BDI deve ter no máximo 6 dígitos inteiros e 4 casas decimais"
        )
        BigDecimal bdiPercentage

) {
}