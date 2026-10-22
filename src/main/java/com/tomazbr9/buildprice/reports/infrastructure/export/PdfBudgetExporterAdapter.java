
package com.tomazbr9.buildprice.reports.infrastructure.export;

import com.tomazbr9.buildprice.reports.application.dto.BudgetReportData;
import com.tomazbr9.buildprice.reports.application.port.out.BudgetPdfExporter;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PdfBudgetExporterAdapter implements BudgetPdfExporter {

    private static final Locale BRAZIL =
            Locale.forLanguageTag("pt-BR");

    private static final Color HEADER_COLOR =
            new Color(35, 55, 80);


    private final PdfFontProvider fontProvider;
    private final Font titleFont;
    private final Font sectionFont;
    private final Font regularFont;
    private final Font headerFont;

    public PdfBudgetExporterAdapter(PdfFontProvider fonts) {
        this.titleFont = fonts.bold(18);
        this.sectionFont = fonts.bold(11);
        this.regularFont = fonts.regular(8);
        this.headerFont = fonts.bold(8, Color.WHITE);
        this.fontProvider = fonts;
    }

    @Override
    public byte[] export(BudgetReportData budget) {

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        Document document = new Document(
                PageSize.A4.rotate(),
                32,
                32,
                40,
                40
        );

        try {

            PdfWriter writer = PdfWriter.getInstance(
                    document,
                    output
            );

            writer.setPageEvent(
                    new BudgetPdfPageEvent(
                            fontProvider.regularBaseFont()
                    )
            );


            document.addTitle("Orçamento - BuildPrice");
            document.addCreator("BuildPrice");

            document.open();

            addHeader(document, budget);
            addItemsTable(document, budget);
            addFinancialSummary(document, budget);

        } catch (DocumentException exception) {
            throw new IllegalStateException(
                    "Não foi possível gerar o PDF do orçamento.",
                    exception
            );
        } finally {
            document.close();
        }

        return output.toByteArray();
    }


    private void addHeader(
            Document document,
            BudgetReportData budget
    ) throws DocumentException {

        Paragraph title = new Paragraph(
                "ORÇAMENTO DE OBRA",
                titleFont
        );

        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(14);

        document.add(title);

        String referenceMonth = budget.referenceMonth()
                .format(
                        DateTimeFormatter.ofPattern(
                                "MMMM/yyyy",
                                BRAZIL
                        )
                );

        String regime = switch (budget.taxReliefRegime()) {
            case "EXEMPTED" -> "Desonerado";
            case "NOT_EXEMPTED" -> "Não desonerado";
            default -> "Não informado";
        };

        Paragraph identification = new Paragraph(
                "Projeto: " + budget.projectName()
                        + "\nOrçamento: " + budget.budgetId()
                        + "\nEstado: " + budget.stateAbbreviation()
                        + "\nReferência SINAPI: " + referenceMonth
                        + "\nRegime: " + regime,
                regularFont
        );

        identification.setSpacingAfter(18);

        document.add(identification);
    }


    private void addItemsTable(
            Document document,
            BudgetReportData budget
    ) throws DocumentException {

        Paragraph section = new Paragraph(
                "DISCRIMINAÇÃO DOS SERVIÇOS",
                sectionFont
        );

        section.setSpacingAfter(10);
        document.add(section);

        PdfPTable table = new PdfPTable(
                new float[]{16, 9, 34, 7, 8, 12, 14}
        );

        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        addHeaderCell(table, "Grupo");
        addHeaderCell(table, "Código");
        addHeaderCell(table, "Descrição");
        addHeaderCell(table, "Un.");
        addHeaderCell(table, "Qtd.");
        addHeaderCell(table, "Preço unit.");
        addHeaderCell(table, "Subtotal");


        Map<UUID, BudgetReportData.GroupData> groupsById =
                budget.groups().stream()
                        .collect(Collectors.toMap(
                                BudgetReportData.GroupData::id,
                                Function.identity()
                        ));

        for (BudgetReportData.GroupData group : budget.groups()) {

            String groupPath = buildGroupPath(
                    group,
                    groupsById
            );

            for (BudgetReportData.ItemData item : group.items()) {

                addCell(table, groupPath, false);
                addCell(table, item.code(), false);
                addCell(table, item.description(), false);
                addCell(table, item.unit(), false);
                addCell(table, number(item.quantity()), true);
                addCell(table, money(item.unitPrice()), true);
                addCell(table, money(item.directCost()), true);
            }
        }


        document.add(table);
    }

    private void addFinancialSummary(
            Document document,
            BudgetReportData budget
    ) throws DocumentException {

        Paragraph title = new Paragraph(
                "RESUMO FINANCEIRO",
                sectionFont
        );

        title.setSpacingBefore(20);
        title.setSpacingAfter(10);

        document.add(title);

        PdfPTable table = new PdfPTable(
                new float[]{70, 30}
        );

        table.setWidthPercentage(55);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addSummaryRow(
                table,
                "Custo direto",
                money(budget.directCost())
        );

        addSummaryRow(
                table,
                "BDI (" + number(budget.bdiPercentage()) + "%)",
                money(budget.bdiValue())
        );

        addSummaryRow(
                table,
                "PREÇO FINAL",
                money(budget.finalPrice())
        );

        document.add(table);
    }

    private void addHeaderCell(
            PdfPTable table,
            String value
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(value, headerFont)
        );

        cell.setBackgroundColor(HEADER_COLOR);
        cell.setPadding(7);

        table.addCell(cell);
    }

    private void addCell(
            PdfPTable table,
            String value,
            boolean alignRight
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        value == null ? "" : value,
                        regularFont
                )
        );

        cell.setPadding(6);

        if (alignRight) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }

        table.addCell(cell);
    }

    private void addSummaryRow(
            PdfPTable table,
            String label,
            String value
    ) {
        addCell(table, label, false);
        addCell(table, value, true);
    }

    private String money(BigDecimal value) {
        NumberFormat format =
                NumberFormat.getCurrencyInstance(BRAZIL);

        return format.format(value);
    }

    private String number(BigDecimal value) {
        NumberFormat format =
                NumberFormat.getNumberInstance(BRAZIL);

        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(4);

        return format.format(value);
    }


    private String buildGroupPath(
            BudgetReportData.GroupData group,
            Map<UUID, BudgetReportData.GroupData> groupsById
    ) {
        LinkedList<String> names =
                new java.util.LinkedList<>();

        Set<UUID> visited =
                new HashSet<>();

        BudgetReportData.GroupData current = group;

        while (current != null) {

            if (!visited.add(current.id())) {
                throw new IllegalStateException(
                        "Foi identificado um ciclo na estrutura da EAP."
                );
            }

            names.addFirst(current.name());

            UUID parentId = current.parentGroupId();

            if (parentId == null) {
                break;
            }

            current = groupsById.get(parentId);

            if (current == null) {
                throw new IllegalStateException(
                        "O grupo pai da EAP não foi encontrado."
                );
            }
        }

        return String.join(" > ", names);
    }

}
