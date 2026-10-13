package com.tomazbr9.buildprice.catalog.api;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CompositionBudgetQuery {

    Optional<CompositionBudgetView> findById(
            UUID compositionId
    );

    record CompositionBudgetView(
            UUID id,
            UUID sinapiTableVersionId,
            String code,
            String description,
            String unit,
            BigDecimal unitCost
    ) {
    }
}