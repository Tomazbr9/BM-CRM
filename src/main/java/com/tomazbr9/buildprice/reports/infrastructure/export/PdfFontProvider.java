
package com.tomazbr9.buildprice.reports.infrastructure.export;

import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.BaseFont;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfFontProvider {

    private final BaseFont regular;
    private final BaseFont bold;

    public PdfFontProvider() {
        this.regular = loadFont("fonts/NotoSans-Regular.ttf");
        this.bold = loadFont("fonts/NotoSans-Bold.ttf");
    }


    public BaseFont regularBaseFont() {
        return regular;
    }

    public Font regular(float size) {
        return new Font(regular, size, Font.NORMAL);
    }

    public Font bold(float size) {
        return new Font(bold, size, Font.NORMAL);
    }

    public Font bold(float size, Color color) {
        return new Font(bold, size, Font.NORMAL, color);
    }

    private BaseFont loadFont(String path) {

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Fonte do PDF não encontrada: " + path
                );
            }

            byte[] bytes = input.readAllBytes();

            return BaseFont.createFont(
                    path,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    bytes,
                    null
            );

        } catch (IOException | DocumentException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar a fonte do PDF.",
                    exception
            );
        }
    }
}
