package com.tomazbr9.buildprice.budget.infrastructure.gateway;

import com.tomazbr9.buildprice.budget.application.port.out.SinapiCatalogGateway;
import com.tomazbr9.buildprice.catalog.api.CompositionBudgetQuery;
import com.tomazbr9.buildprice.catalog.api.SinapiTableVersionExistsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SinapiCatalogGatewayAdapter
        implements SinapiCatalogGateway {

    private final SinapiTableVersionExistsQuery versionQuery;
    private final CompositionBudgetQuery compositionQuery;

    @Override
    public boolean versionExists(UUID versionId) {
        return versionQuery.existsById(versionId);
    }

    @Override
    public Optional<CompositionData> findCompositionById(
            UUID compositionId
    ) {
        return compositionQuery
                .findById(compositionId)
                .map(composition ->
                        new CompositionData(
                                composition.id(),
                                composition.sinapiTableVersionId(),
                                composition.unitCost()
                        )
                );
    }
}