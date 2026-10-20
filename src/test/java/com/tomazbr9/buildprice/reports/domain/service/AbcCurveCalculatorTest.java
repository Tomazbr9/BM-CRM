
package com.tomazbr9.buildprice.reports.domain.service;

import com.tomazbr9.buildprice.reports.domain.enums.AbcClassification;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurve;
import com.tomazbr9.buildprice.reports.domain.model.AbcCurveEntry;

import com.tomazbr9.buildprice.reports.domain.model.AbcCurveItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbcCurveCalculatorTest {

    private AbcCurveCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AbcCurveCalculator();
    }

    @Test
    void shouldSortItemsByDescendingDirectCost() {

        List<AbcCurveEntry> entries = List.of(
                item("Menor custo", "100.00"),
                item("Maior custo", "600.00"),
                item("Custo intermediário", "300.00")
        );

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                entries
        );

        assertEquals(3, result.items().size());

        assertEquals(
                "Maior custo",
                result.items().get(0).entry().description()
        );

        assertEquals(
                "Custo intermediário",
                result.items().get(1).entry().description()
        );

        assertEquals(
                "Menor custo",
                result.items().get(2).entry().description()
        );
    }

    @Test
    void shouldCalculateParticipationAndAccumulatedPercentage() {

        List<AbcCurveEntry> entries = List.of(
                item("Fundação", "5000.00"),
                item("Alvenaria", "3000.00"),
                item("Pintura", "1500.00"),
                item("Transporte", "500.00")
        );

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                entries
        );

        assertMoney("10000.00", result.totalDirectCost());

        assertMoney(
                "50.00",
                result.items().get(0).participationPercentage()
        );

        assertMoney(
                "80.00",
                result.items().get(1).accumulatedPercentage()
        );

        assertMoney(
                "95.00",
                result.items().get(2).accumulatedPercentage()
        );

        assertMoney(
                "100.00",
                result.items().get(3).accumulatedPercentage()
        );
    }

    @Test
    void shouldHandleEmptyBudget() {

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                List.of()
        );

        assertMoney("0.00", result.totalDirectCost());

        assertTrue(result.items().isEmpty());
    }

    @Test
    void shouldHandleZeroTotalCost() {

        List<AbcCurveEntry> entries = List.of(
                item("Item sem custo", "0.00")
        );

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                entries
        );

        assertMoney("0.00", result.totalDirectCost());

        assertMoney(
                "0.00",
                result.items().getFirst().participationPercentage()
        );

        assertMoney(
                "0.00",
                result.items().getFirst().accumulatedPercentage()
        );
    }

    @Test
    void shouldApplyHalfEvenRounding() {

        List<AbcCurveEntry> entries = List.of(
                item("Item A", "1.00"),
                item("Item B", "31.00")
        );

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                entries
        );

        // 1 / 32 * 100 = 3.125
        // HALF_EVEN => 3.12
        assertMoney(
                "3.12",
                result.items().getLast().participationPercentage()
        );

        assertMoney(
                "100.00",
                result.items().getLast().accumulatedPercentage()
        );
    }

    @Test
    void shouldKeepItemsWithSameCost() {

        List<AbcCurveEntry> entries = List.of(
                item("Item A", "100.00"),
                item("Item B", "100.00")
        );

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                entries
        );

        assertEquals(2, result.items().size());

        assertMoney(
                "50.00",
                result.items().get(0).participationPercentage()
        );

        assertMoney(
                "50.00",
                result.items().get(1).participationPercentage()
        );

        assertMoney(
                "100.00",
                result.items().getLast().accumulatedPercentage()
        );
    }


    @Test
    void shouldClassifyItemsAsA_B_AndC() {

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                List.of(
                        item("Estrutura", "4000.00"),
                        item("Fundação", "2500.00"),
                        item("Alvenaria", "1500.00"),
                        item("Instalações", "1000.00"),
                        item("Pintura", "500.00"),
                        item("Limpeza", "300.00"),
                        item("Transporte", "200.00")
                )
        );

        assertEquals(
                List.of(
                        AbcClassification.A,
                        AbcClassification.A,
                        AbcClassification.A,
                        AbcClassification.B,
                        AbcClassification.B,
                        AbcClassification.C,
                        AbcClassification.C
                ),
                result.items().stream()
                        .map(AbcCurveItem::classification)
                        .toList()
        );
    }

    @Test
    void shouldKeepBoundaryCrossingItemInPreviousClass() {

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                List.of(
                        item("Item A", "750.00"),
                        item("Item B", "120.00"),
                        item("Item C", "80.00"),
                        item("Item D", "50.00")
                )
        );

        // 75% -> 87%: continua A
        assertEquals(
                AbcClassification.A,
                result.items().get(1).classification()
        );

        // 87% -> 95%: classe B
        assertEquals(
                AbcClassification.B,
                result.items().get(2).classification()
        );

        // 95% -> 100%: classe C
        assertEquals(
                AbcClassification.C,
                result.items().get(3).classification()
        );
    }

    @Test
    void shouldClassifyEqualCostItemsTogether() {

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                List.of(
                        item("Maior custo", "700.00"),
                        item("Empatado A", "100.00"),
                        item("Empatado B", "100.00"),
                        item("Menor custo", "100.00")
                )
        );

        // Os três itens de R$ 100 começam
        // com acumulado anterior de 70%.
        assertTrue(
                result.items().stream()
                        .skip(1)
                        .allMatch(
                                entry -> entry.classification()
                                        == AbcClassification.A
                        )
        );
    }

    @Test
    void shouldNotClassifyZeroCostItems() {

        AbcCurve result = calculator.calculate(
                UUID.randomUUID(),
                List.of(
                        item("Item com custo", "100.00"),
                        item("Item sem custo", "0.00")
                )
        );

        assertEquals(
                AbcClassification.A,
                result.items().getFirst().classification()
        );

        assertNull(
                result.items().getLast().classification()
        );
    }


    private AbcCurveEntry item(
            String description,
            String directCost
    ) {

        BigDecimal cost = new BigDecimal(directCost);

        return new AbcCurveEntry(
                UUID.randomUUID(),
                "MANUAL",
                null,
                description,
                "UN",
                BigDecimal.ONE,
                cost,
                cost
        );
    }

    private void assertMoney(
            String expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}
