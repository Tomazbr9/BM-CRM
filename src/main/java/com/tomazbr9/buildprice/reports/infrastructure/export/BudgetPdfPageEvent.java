
package com.tomazbr9.buildprice.reports.infrastructure.export;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.Phrase;

import java.awt.Color;

public class BudgetPdfPageEvent extends PdfPageEventHelper {

    private static final float FONT_SIZE = 8f;
    private static final float TEMPLATE_WIDTH = 35f;

    private final BaseFont font;

    private PdfTemplate totalPagesTemplate;

    public BudgetPdfPageEvent(BaseFont font) {
        this.font = font;
    }

    @Override
    public void onOpenDocument(
            PdfWriter writer,
            Document document
    ) {
        totalPagesTemplate = writer
                .getDirectContent()
                .createTemplate(TEMPLATE_WIDTH, 12);
    }

    @Override
    public void onEndPage(
            PdfWriter writer,
            Document document
    ) {

        PdfContentByte canvas = writer.getDirectContent();

        float left = document.left();
        float right = document.right();
        float y = document.bottom() - 20;

        canvas.saveState();

        canvas.setColorStroke(new Color(210, 215, 220));
        canvas.setLineWidth(0.5f);

        canvas.moveTo(left, y + 15);
        canvas.lineTo(right, y + 15);
        canvas.stroke();

        canvas.setColorFill(new Color(90, 100, 110));

        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_LEFT,
                new Phrase(
                        "BuildPrice | Orçamento de obra",
                        new org.openpdf.text.Font(font, FONT_SIZE)
                ),
                left,
                y,
                0
        );

        String pageText =
                "Página " + writer.getPageNumber() + " de ";

        float textWidth = font.getWidthPoint(
                pageText,
                FONT_SIZE
        );

        float startX = right - textWidth - TEMPLATE_WIDTH;

        canvas.beginText();
        canvas.setFontAndSize(font, FONT_SIZE);
        canvas.setTextMatrix(startX, y);
        canvas.showText(pageText);
        canvas.endText();

        canvas.addTemplate(
                totalPagesTemplate,
                startX + textWidth,
                y - 2
        );

        canvas.restoreState();
    }

    @Override
    public void onCloseDocument(
            PdfWriter writer,
            Document document
    ) {

        int totalPages = writer.getPageNumber() - 1;

        totalPagesTemplate.beginText();

        totalPagesTemplate.setFontAndSize(
                font,
                FONT_SIZE
        );

        totalPagesTemplate.setColorFill(
                new Color(90, 100, 110)
        );

        totalPagesTemplate.setTextMatrix(0, 2);

        totalPagesTemplate.showText(
                String.valueOf(totalPages)
        );

        totalPagesTemplate.endText();
    }
}
