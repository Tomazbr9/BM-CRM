package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddManualBudgetItemRequest(

        @NotBlank(message = "A descrição é obrigatória")
        @Size(
                max = 255,
                message = "A descrição deve ter no máximo 255 caracteres"
        )
        String description,

        @NotBlank(message = "A unidade é obrigatória")
        @Size(
                max = 50,
                message = "A unidade deve ter no máximo 50 caracteres"
        )
        String unit,

        @NotNull(message = "O preço unitário é obrigatório")
        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "O preço unitário não pode ser negativo"
        )
        BigDecimal unitPrice,

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(
                value = "0.0001",
                message = "A quantidade deve ser maior que zero"
        )
        BigDecimal quantity

) {
}