package com.tomazbr9.buildprice.catalog.application.usecase;

import com.tomazbr9.buildprice.catalog.api.CompositionBudgetQuery;
import com.tomazbr9.buildprice.catalog.application.port.out.CompositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CompositionBudgetQueryImpl
        implements CompositionBudgetQuery {

    private final CompositionRepository compositionRepository;

    public CompositionBudgetQueryImpl(
            CompositionRepository compositionRepository
    ) {
        this.compositionRepository = compositionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompositionBudgetView> findById(
            UUID compositionId
    ) {

        return compositionRepository
                .findById(compositionId)
                .map(composition ->
                        new CompositionBudgetView(
                                composition.getId(),
                                composition.getSinapiTableVersionId(),
                                composition.getUnitCost()
                        )
                );
    }
}