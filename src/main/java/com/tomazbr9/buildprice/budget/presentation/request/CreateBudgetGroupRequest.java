package com.tomazbr9.buildprice.budget.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBudgetGroupRequest(

        UUID parentGroupId,

        @NotBlank(message = "Group name is required")
        @Size(
                max = 150,
                message = "Group name must have at most 150 characters"
        )
        String name,

        @Min(
                value = 0,
                message = "Sort order cannot be negative"
        )
        Integer sortOrder
) {
}