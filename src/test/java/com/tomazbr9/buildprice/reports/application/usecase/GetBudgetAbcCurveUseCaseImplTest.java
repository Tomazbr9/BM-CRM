package com.tomazbr9.buildprice.reports.application.usecase;

import com.tomazbr9.buildprice.reports.application.dto.AbcCurveResult;
import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;
import com.tomazbr9.buildprice.reports.application.port.out.BudgetReportGateway;
import com.tomazbr9.buildprice.reports.domain.enums.AbcClassification;
import com.tomazbr9.buildprice.reports.domain.service.AbcCurveCalculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBudgetAbcCurveUseCaseImplTest {

    @Mock
    private BudgetReportGateway budgetReportGateway;

    private GetBudgetAbcCurveUseCaseImpl useCase;

    private final AbcCurveCalculator calculator =
            new AbcCurveCalculator();

    @BeforeEach
    void setUp() {
        useCase = new GetBudgetAbcCurveUseCaseImpl(
                budgetReportGateway,
                calculator
        );
    }

    @Test
    void shouldGenerateAbcCurveForBudget() {

        UUID budgetId = UUID.randomUUID();

        BudgetReportData.ItemData itemA =
                new BudgetReportData.ItemData(
                        UUID.randomUUID(),
                        "SINAPI",
                        "103689",
                        "Fundação",
                        "M3",
                        BigDecimal.ONE,
                        new BigDecimal("5000.00"),
                        new BigDecimal("5000.00")
                );

        BudgetReportData.ItemData itemB =
                new BudgetReportData.ItemData(
                        UUID.randomUUID(),
                        "MANUAL",
                        null,
                        "Transporte",
                        "UN",
                        BigDecimal.ONE,
                        new BigDecimal("500.00"),
                        new BigDecimal("500.00")
                );

        BudgetReportData budget = new BudgetReportData(
                budgetId,
                "Residencial Jardim",
                new BigDecimal("20.00"),
                new BigDecimal("5500.00"),
                new BigDecimal("1100.00"),
                new BigDecimal("6600.00"),
                List.of(
                        new BudgetReportData.GroupData(
                                UUID.randomUUID(),
                                null,
                                "Serviços",
                                0,
                                List.of(itemB, itemA)
                        )
                )
        );

        when(budgetReportGateway.getByBudgetId(budgetId))
                .thenReturn(budget);

        AbcCurveResult result = useCase.execute(budgetId);

        assertEquals(budgetId, result.budgetId());
        assertEquals(2, result.items().size());

        assertEquals(
                itemA.id(),
                result.items().getFirst().budgetItemId()
        );

        assertEquals(
                0,
                new BigDecimal("5500.00")
                        .compareTo(result.totalDirectCost())
        );

        assertEquals(
                0,
                new BigDecimal("90.91")
                        .compareTo(
                                result.items()
                                        .getFirst()
                                        .participationPercentage()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(
                                result.items()
                                        .getLast()
                                        .accumulatedPercentage()
                        )
        );

        assertEquals(
                AbcClassification.A,
                result.items().getFirst().classification()
        );

        assertEquals(
                AbcClassification.B,
                result.items().getLast().classification()
        );

        verify(budgetReportGateway).getByBudgetId(budgetId);
        verifyNoMoreInteractions(budgetReportGateway);
    }
}
