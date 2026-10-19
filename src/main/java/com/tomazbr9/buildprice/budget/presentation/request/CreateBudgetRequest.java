package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBudgetRequest(

        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId,

        @NotNull(message = "O ID da versão da tabela SINAPI é obrigatório")
        UUID sinapiTableVersionId

) {
}