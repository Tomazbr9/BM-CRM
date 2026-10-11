package com.tomazbr9.buildprice.budget.infrastructure.mapper;

import com.tomazbr9.buildprice.budget.domain.entity.ManualItem;
import com.tomazbr9.buildprice.budget.infrastructure.entity.ManualItemJpaEntity;

public final class ManualItemMapper {

    private ManualItemMapper() {
    }

    public static ManualItemJpaEntity toJpaEntity(
            ManualItem item
    ) {
        return new ManualItemJpaEntity(
                item.getId(),
                item.getBudgetId(),
                item.getDescription(),
                item.getUnit(),
                item.getUnitPrice()
        );
    }

    public static ManualItem toDomain(
            ManualItemJpaEntity entity
    ) {
        return ManualItem.restore(
                entity.getId(),
                entity.getBudgetId(),
                entity.getDescription(),
                entity.getUnit(),
                entity.getUnitPrice()
        );
    }
}