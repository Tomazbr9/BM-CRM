package com.tomazbr9.buildprice.reports.infrastructure.export;

import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PdfBudgetExporterAdapterTest {

    private final PdfBudgetExporterAdapter exporter =
            new PdfBudgetExporterAdapter(new PdfFontProvider());

    @Test
    void shouldGenerateValidPdf() throws Exception {

        BudgetReportData.ItemData item =
                new BudgetReportData.ItemData(
                        UUID.randomUUID(),
                        "SINAPI",
                        "103689",
                        "Execução de alvenaria",
                        "M2",
                        new BigDecimal("10.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("1000.00")
                );

        BudgetReportData.GroupData group =
                new BudgetReportData.GroupData(
                        UUID.randomUUID(),
                        null,
                        "Alvenaria",
                        0,
                        List.of(item)
                );

        BudgetReportData budget = new BudgetReportData(
                UUID.randomUUID(),
                "Residencial Jardim",
                "SP",
                YearMonth.of(2026, 9),
                "NOT_EXEMPTED",
                new BigDecimal("20.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1200.00"),
                List.of(group)
        );

        byte[] pdf = exporter.export(budget);

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);

        String signature = new String(
                pdf,
                0,
                5,
                StandardCharsets.US_ASCII
        );

        assertEquals("%PDF-", signature);

        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() >= 1);
        }
    }

    @Test
    void shouldGeneratePdfForEmptyBudget() throws Exception {

        BudgetReportData budget = new BudgetReportData(
                UUID.randomUUID(),
                "Residencial Jardim",
                "SP",
                YearMonth.of(2026, 9),
                "NOT_EXEMPTED",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of()
        );

        byte[] pdf = exporter.export(budget);

        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() >= 1);
        }
    }


    @Test
    void shouldGenerateMultiplePagesWithPagination()
            throws Exception {

        List<BudgetReportData.ItemData> items =
                new ArrayList<>();

        for (int i = 1; i <= 150; i++) {

            items.add(new BudgetReportData.ItemData(
                    UUID.randomUUID(),
                    "MANUAL",
                    null,
                    "Serviço de construção número " + i,
                    "UN",
                    BigDecimal.ONE,
                    new BigDecimal("100.00"),
                    new BigDecimal("100.00")
            ));
        }

        BudgetReportData.GroupData group =
                new BudgetReportData.GroupData(
                        UUID.randomUUID(),
                        null,
                        "Serviços gerais",
                        0,
                        items
                );

        BudgetReportData budget = new BudgetReportData(
                UUID.randomUUID(),
                "Residencial Jardim",
                "SP",
                YearMonth.of(2026, 9),
                "NOT_EXEMPTED",
                new BigDecimal("20.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("18000.00"),
                List.of(group)
        );

        byte[] pdf = exporter.export(budget);

        try (PdfReader reader = new PdfReader(pdf)) {

            assertTrue(
                    reader.getNumberOfPages() > 1,
                    "O PDF deveria possuir múltiplas páginas."
            );
        }
    }

}
