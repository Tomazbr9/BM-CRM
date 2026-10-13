package com.tomazbr9.buildprice.catalog.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CompositionBudgetBatchQuery {

    List<CompositionBudgetView> findByIds(
            Collection<UUID> compositionIds
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