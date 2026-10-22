package com.tomazbr9.buildprice.reports.infrastructure.export;

import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

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
    void shouldGenerateValidPdfWithBudgetInformation() throws Exception {

        BudgetReportData.ItemData item =
                item("Execução de alvenaria", "1000.00");

        BudgetReportData.GroupData group =
                group(null, "Alvenaria", List.of(item));

        BudgetReportData budget = budget(
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1200.00"),
                List.of(group)
        );

        byte[] pdf = exporter.export(budget);

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);

        assertEquals(
                "%PDF-",
                new String(pdf, 0, 5, StandardCharsets.US_ASCII)
        );

        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() >= 1);
        }

        String text = extractPdfText(pdf);

        assertAll(
                () -> assertTrue(text.contains("ORÇAMENTO DE OBRA")),
                () -> assertTrue(text.contains("Residencial Jardim")),
                () -> assertTrue(text.contains("Estado: SP")),
                () -> assertTrue(text.contains("setembro/2026")),
                () -> assertTrue(text.contains("Não desonerado")),
                () -> assertTrue(text.contains("Execução de alvenaria")),
                () -> assertTrue(text.contains("Custo direto")),
                () -> assertTrue(text.contains("BDI")),
                () -> assertTrue(text.contains("1.000,00")),
                () -> assertTrue(text.contains("1.200,00"))
        );
    }

    @Test
    void shouldGeneratePdfForEmptyBudget() throws Exception {

        BudgetReportData budget = budget(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of()
        );

        byte[] pdf = exporter.export(budget);

        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() >= 1);
        }

        String text = extractPdfText(pdf);

        assertAll(
                () -> assertTrue(text.contains("Residencial Jardim")),
                () -> assertTrue(text.contains("RESUMO FINANCEIRO")),
                () -> assertTrue(text.contains("Custo direto")),
                () -> assertTrue(text.contains("PREÇO FINAL"))
        );
    }

    @Test
    void shouldGenerateMultiplePagesWithPagination() throws Exception {

        List<BudgetReportData.ItemData> items = new ArrayList<>();

        for (int i = 1; i <= 150; i++) {
            items.add(
                    item(
                            "Serviço de construção número " + i,
                            "100.00"
                    )
            );
        }

        BudgetReportData.GroupData group =
                group(null, "Serviços gerais", items);

        BudgetReportData budget = budget(
                new BigDecimal("15000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("18000.00"),
                List.of(group)
        );

        byte[] pdf = exporter.export(budget);

        try (PdfReader reader = new PdfReader(pdf)) {

            int totalPages = reader.getNumberOfPages();

            assertTrue(
                    totalPages > 1,
                    "O PDF deveria possuir múltiplas páginas."
            );

            PdfTextExtractor extractor =
                    new PdfTextExtractor(reader);

            for (int page = 1; page <= totalPages; page++) {

                String text = extractor.getTextFromPage(page);

                assertTrue(
                        text.contains("BuildPrice"),
                        "Rodapé ausente na página " + page
                );

                // A extração de texto pode separar o número
                // atual e o total gerado pelo PdfTemplate.
                String normalized = text.replaceAll("\\s+", " ");

                assertTrue(
                        normalized.contains("Página " + page + " de"),
                        "Numeração ausente na página " + page
                );
            }
        }

        String fullText = extractPdfText(pdf);

        assertTrue(
                fullText.contains("Serviço de construção número 150"),
                "O último item deveria aparecer no PDF."
        );

        assertTrue(fullText.contains("18.000,00"));
    }

    @Test
    void shouldPreserveBudgetGroupHierarchy() throws Exception {

        UUID parentGroupId = UUID.randomUUID();

        BudgetReportData.GroupData parentGroup =
                new BudgetReportData.GroupData(
                        parentGroupId,
                        null,
                        "Fundação",
                        0,
                        List.of()
                );

        BudgetReportData.GroupData childGroup =
                new BudgetReportData.GroupData(
                        UUID.randomUUID(),
                        parentGroupId,
                        "Concreto",
                        1,
                        List.of(item("Execução de concreto", "500.00"))
                );

        BudgetReportData budget = budget(
                new BigDecimal("500.00"),
                new BigDecimal("100.00"),
                new BigDecimal("600.00"),
                List.of(parentGroup, childGroup)
        );

        String text = extractPdfText(exporter.export(budget));

        assertAll(
                () -> assertTrue(text.contains("Fundação")),
                () -> assertTrue(text.contains("Concreto")),
                () -> assertTrue(text.contains("Execução de concreto"))
        );
    }

    private String extractPdfText(byte[] pdf) throws Exception {

        try (PdfReader reader = new PdfReader(pdf)) {

            PdfTextExtractor extractor =
                    new PdfTextExtractor(reader);

            StringBuilder text = new StringBuilder();

            for (int page = 1;
                 page <= reader.getNumberOfPages();
                 page++) {

                text.append(extractor.getTextFromPage(page));
                text.append("\n");
            }

            return text.toString();
        }
    }

    private BudgetReportData.ItemData item(
            String description,
            String directCost
    ) {
        BigDecimal cost = new BigDecimal(directCost);

        return new BudgetReportData.ItemData(
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

    private BudgetReportData.GroupData group(
            UUID parentGroupId,
            String name,
            List<BudgetReportData.ItemData> items
    ) {
        return new BudgetReportData.GroupData(
                UUID.randomUUID(),
                parentGroupId,
                name,
                0,
                items
        );
    }

    private BudgetReportData budget(
            BigDecimal directCost,
            BigDecimal bdiValue,
            BigDecimal finalPrice,
            List<BudgetReportData.GroupData> groups
    ) {
        return new BudgetReportData(
                UUID.randomUUID(),
                "Residencial Jardim",
                "SP",
                YearMonth.of(2026, 9),
                "NOT_EXEMPTED",
                new BigDecimal("20.00"),
                directCost,
                bdiValue,
                finalPrice,
                groups
        );
    }
}
