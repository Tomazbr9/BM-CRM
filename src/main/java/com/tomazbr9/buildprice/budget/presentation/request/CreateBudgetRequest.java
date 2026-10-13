package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBudgetRequest(

        @NotNull(message = "Project id is required")
        UUID projectId,

        @NotNull(message = "SINAPI table version id is required")
        UUID sinapiTableVersionId

) {
}