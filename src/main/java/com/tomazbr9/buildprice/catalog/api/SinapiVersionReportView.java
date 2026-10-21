package com.tomazbr9.buildprice.catalog.api;

import java.time.YearMonth;
import java.util.UUID;

public record SinapiVersionReportView(
        UUID versionId,
        String stateAbbreviation,
        YearMonth referenceMonth,
        String taxReliefRegime
) {
}
