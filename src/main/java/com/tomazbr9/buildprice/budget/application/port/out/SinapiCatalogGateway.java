package com.tomazbr9.buildprice.budget.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface SinapiCatalogGateway {

    boolean versionExists(UUID versionId);

    Optional<CompositionData> findCompositionById(
            UUID compositionId
    );

    record CompositionData(
            UUID id,
            UUID versionId,
            BigDecimal unitCost
    ) {
    }
}