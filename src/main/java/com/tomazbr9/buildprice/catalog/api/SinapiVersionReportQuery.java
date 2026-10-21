package com.tomazbr9.buildprice.catalog.api;

import java.util.Optional;
import java.util.UUID;

public interface SinapiVersionReportQuery {

    Optional<SinapiVersionReportView> findById(UUID versionId);
}
