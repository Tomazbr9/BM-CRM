
package com.tomazbr9.buildprice.reports.domain.service;

import com.tomazbr9.buildprice.reports.domain.enums.AbcClassification;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurve;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurveEntry;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurveItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AbcCurveCalculator {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private static final BigDecimal CLASS_A_LIMIT =
            new BigDecimal("0.80");

    private static final BigDecimal CLASS_B_LIMIT =
            new BigDecimal("0.95");

    public AbcCurve calculate(
            UUID budgetId,
            List<AbcCurveEntry> entries
    ) {
        List<AbcCurveEntry> sortedEntries = entries.stream()
                .sorted(
                        Comparator.comparing(
                                        AbcCurveEntry::directCost
                                ).reversed()
                                .thenComparing(AbcCurveEntry::budgetItemId)
                )
                .toList();

        BigDecimal total = sortedEntries.stream()
                .map(AbcCurveEntry::directCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal accumulatedCost = BigDecimal.ZERO;

        List<AbcCurveItem> results = new ArrayList<>();

        int index = 0;

        while (index < sortedEntries.size()) {

            BigDecimal currentCost =
                    sortedEntries.get(index).directCost();

            // Todos os itens com o mesmo custo recebem
            // a mesma classificação.
            int groupEnd = index + 1;

            while (
                    groupEnd < sortedEntries.size()
                            && sortedEntries.get(groupEnd)
                            .directCost()
                            .compareTo(currentCost) == 0
            ) {
                groupEnd++;
            }

            AbcClassification classification = classify(
                    currentCost,
                    accumulatedCost,
                    total
            );

            for (int i = index; i < groupEnd; i++) {

                AbcCurveEntry entry = sortedEntries.get(i);

                accumulatedCost = accumulatedCost.add(
                        entry.directCost()
                );

                results.add(
                        new AbcCurveItem(
                                entry,
                                percentage(entry.directCost(), total),
                                percentage(accumulatedCost, total),
                                classification
                        )
                );
            }

            index = groupEnd;
        }

        return new AbcCurve(
                budgetId,
                total.setScale(2, RoundingMode.HALF_EVEN),
                results
        );
    }

    private AbcClassification classify(
            BigDecimal itemCost,
            BigDecimal accumulatedBefore,
            BigDecimal total
    ) {
        if (total.signum() == 0 || itemCost.signum() == 0) {
            return null;
        }

        // Compara valores monetários exatos,
        // sem arredondar percentuais intermediários.
        if (accumulatedBefore.compareTo(
                total.multiply(CLASS_A_LIMIT)
        ) < 0) {
            return AbcClassification.A;
        }

        if (accumulatedBefore.compareTo(
                total.multiply(CLASS_B_LIMIT)
        ) < 0) {
            return AbcClassification.B;
        }

        return AbcClassification.C;
    }

    private BigDecimal percentage(
            BigDecimal value,
            BigDecimal total
    ) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return value
                .multiply(ONE_HUNDRED)
                .divide(total, 2, RoundingMode.HALF_EVEN);
    }
}
