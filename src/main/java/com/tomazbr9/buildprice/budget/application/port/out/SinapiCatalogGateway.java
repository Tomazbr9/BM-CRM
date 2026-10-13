package com.tomazbr9.buildprice.budget.application.port.out;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SinapiCatalogGateway {

    boolean versionExists(UUID versionId);

    Optional<CompositionData> findCompositionById(
            UUID compositionId
    );

    List<CompositionData> findCompositionsByIds(
            Collection<UUID> compositionIds
    );

    record CompositionData(
            UUID id,
            UUID versionId,
            String code,
            String description,
            String unit,
            BigDecimal unitCost
    ) {
    }
}