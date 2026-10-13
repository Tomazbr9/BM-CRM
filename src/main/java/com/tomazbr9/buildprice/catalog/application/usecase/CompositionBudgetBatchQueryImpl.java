package com.tomazbr9.buildprice.catalog.application.usecase;

import com.tomazbr9.buildprice.catalog.api.CompositionBudgetBatchQuery;
import com.tomazbr9.buildprice.catalog.application.port.out.CompositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class CompositionBudgetBatchQueryImpl
        implements CompositionBudgetBatchQuery {

    private final CompositionRepository compositionRepository;

    public CompositionBudgetBatchQueryImpl(
            CompositionRepository compositionRepository
    ) {
        this.compositionRepository = compositionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompositionBudgetView> findByIds(
            Collection<UUID> compositionIds
    ) {

        if (compositionIds == null
                || compositionIds.isEmpty()) {

            return List.of();
        }

        return compositionRepository
                .findByIds(compositionIds)
                .stream()
                .map(composition ->
                        new CompositionBudgetView(
                                composition.getId(),
                                composition.getSinapiTableVersionId(),
                                composition.getCode(),
                                composition.getDescription(),
                                composition.getUnit(),
                                composition.getUnitCost()
                        )
                )
                .toList();
    }
}