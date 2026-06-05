package com.chicamax.sentinella.reports.infrastructure.render;

import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ReportFileRenderer {

    public byte[] render(GenerateReportCommand command) {
        return switch (command.format()) {
            case PDF -> renderPdf(command);
            case EXCEL -> renderExcel(command);
        };
    }

    private static byte[] renderPdf(GenerateReportCommand command) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(50, 750);
                cs.showText(pdfSafe("Sentinella - " + command.type().name()));
                cs.endText();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(50, 710);
                cs.showText(pdfSafe("Rango: " + command.from() + " - " + command.to()));
                cs.endText();
                float y = 680;
                for (String line : bodyLines(command)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.newLineAtOffset(50, y);
                    cs.showText(pdfSafe(line.length() > 100 ? line.substring(0, 100) : line));
                    cs.endText();
                    y -= 16;
                }
            }
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generando PDF", e);
        }
    }

    private static byte[] renderExcel(GenerateReportCommand command) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sh = wb.createSheet("Reporte");
            int r = 0;
            Row h0 = sh.createRow(r++);
            h0.createCell(0).setCellValue("Sentinella");
            Row h1 = sh.createRow(r++);
            h1.createCell(0).setCellValue("Tipo");
            h1.createCell(1).setCellValue(command.type().name());
            Row h2 = sh.createRow(r++);
            h2.createCell(0).setCellValue("Desde");
            h2.createCell(1).setCellValue(command.from().toString());
            Row h3 = sh.createRow(r++);
            h3.createCell(0).setCellValue("Hasta");
            h3.createCell(1).setCellValue(command.to().toString());
            for (String line : bodyLines(command)) {
                Row row = sh.createRow(r++);
                row.createCell(0).setCellValue(line);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generando Excel", e);
        }
    }

    private static String[] bodyLines(GenerateReportCommand command) {
        return new String[] {
                "Formato: " + command.format() + " | Tipo: " + command.type().name(),
                "Generado: " + java.time.OffsetDateTime.now(),
                "Tranque: " + command.tailingDamId()
        };
    }

    private static String pdfSafe(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace('\u2014', '-').replace('\u2013', '-');
        StringBuilder sb = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c >= 32 && c <= 126) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
