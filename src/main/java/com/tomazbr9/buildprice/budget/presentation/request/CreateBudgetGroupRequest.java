package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBudgetGroupRequest(

        UUID parentGroupId,

        @NotBlank(message = "O nome do grupo é obrigatório")
        @Size(
                max = 150,
                message = "O nome do grupo deve ter no máximo 150 caracteres"
        )
        String name,

        @Min(
                value = 0,
                message = "A ordem de classificação não pode ser negativa"
        )
        Integer sortOrder
) {
}