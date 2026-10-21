
package com.tomazbr9.buildprice.catalog.application.usecase;

import com.tomazbr9.buildprice.catalog.api.SinapiVersionReportQuery;
import com.tomazbr9.buildprice.catalog.api.SinapiVersionReportView;
import com.tomazbr9.buildprice.catalog.application.port.out.SinapiTableVersionRepository;
import com.tomazbr9.buildprice.catalog.application.port.out.StateRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SinapiVersionReportQueryImpl
        implements SinapiVersionReportQuery {

    private final SinapiTableVersionRepository versionRepository;
    private final StateRepository stateRepository;

    public SinapiVersionReportQueryImpl(
            SinapiTableVersionRepository versionRepository,
            StateRepository stateRepository
    ) {
        this.versionRepository = versionRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SinapiVersionReportView> findById(
            UUID versionId
    ) {
        return versionRepository.findById(versionId)
                .map(version -> {

                    var state = stateRepository
                            .findById(version.getStateId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Estado vinculado à versão SINAPI não encontrado."
                                    )
                            );

                    return new SinapiVersionReportView(
                            version.getId(),
                            state.getStateAbbreviation(),
                            version.getReferenceMonth(),
                            version.getTaxReliefRegime().name()
                    );
                });
    }
}
