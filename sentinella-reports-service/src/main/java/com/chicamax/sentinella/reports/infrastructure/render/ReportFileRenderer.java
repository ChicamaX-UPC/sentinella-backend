package com.chicamax.sentinella.reports.infrastructure.render;

import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataset;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataset.AlertLine;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataset.ReadingStatsLine;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataset.RoundLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ReportFileRenderer {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] TELEMETRY_HEADERS = {"Nodo", "Sensor", "N", "Min", "Max", "Prom", "Und", "Estado"};
    private static final String[] ALERT_HEADERS = {"Id", "Nodo", "Sensor", "Severidad", "Valor", "Estado"};
    private static final String[] ROUND_HEADERS = {"Id", "Programada", "Completada", "Estado", "Operador"};

    public byte[] render(GenerateReportCommand command, ReportDataset data) {
        return switch (command.format()) {
            case PDF -> renderPdf(command, data);
            case EXCEL -> renderExcel(command, data);
        };
    }

    private static byte[] renderPdf(GenerateReportCommand command, ReportDataset data) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(doc);
            writer.drawReportHeader(command, data);
            writer.drawSummary(data);

            if (!data.readingStats().isEmpty()) {
                writer.sectionTitle("Telemetria por nodo (rango seleccionado)");
                writer.drawTelemetryTable(data.readingStats());
            }
            if (!data.alerts().isEmpty()) {
                writer.sectionTitle("Alertas");
                writer.drawAlertsTable(data.alerts());
            }
            if (!data.rounds().isEmpty()) {
                writer.sectionTitle("Rondas de inspeccion");
                writer.drawRoundsTable(data.rounds());
            }
            if (data.readingStats().isEmpty() && data.alerts().isEmpty() && data.rounds().isEmpty()) {
                writer.paragraph("Sin datos en el rango indicado. Amplia las fechas o verifica el seed demo.");
            }
            writer.finish();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generando PDF", e);
        }
    }

    private static byte[] renderExcel(GenerateReportCommand command, ReportDataset data) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle tableHeader = tableHeaderStyle(wb);
            CellStyle sectionTitle = sectionTitleStyle(wb);

            if (useSeparateExcelSheets(command, data)) {
                writeExcelMultiSheet(wb, command, data, tableHeader, sectionTitle);
            } else {
                Sheet sheet = wb.createSheet("Reporte");
                int row = writeExcelSummary(sheet, 0, command, data);
                row = blankRow(sheet, row);
                row = writeExcelAlertsSection(sheet, row, data, tableHeader, sectionTitle);
                if (!data.readingStats().isEmpty()) {
                    row = blankRow(sheet, row);
                    row = writeExcelTelemetrySection(sheet, row, data, tableHeader, sectionTitle);
                }
                if (!data.rounds().isEmpty()) {
                    row = blankRow(sheet, row);
                    writeExcelRoundsSection(sheet, row, data, tableHeader, sectionTitle);
                }
                autosize(sheet, maxExcelColumns(data));
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error generando Excel", e);
        }
    }

    /**
     * OEFA: resumen + alertas + telemetría en hojas distintas (aunque sean solo dos tablas).
     * Otros tipos: varias hojas solo si hay más de dos tablas de datos.
     */
    private static boolean useSeparateExcelSheets(GenerateReportCommand command, ReportDataset data) {
        if (command.type() == ReportType.REGULATORY_OEFA) {
            return true;
        }
        return countExcelDataTables(data) > 2;
    }

    private static void writeExcelMultiSheet(
            XSSFWorkbook wb,
            GenerateReportCommand command,
            ReportDataset data,
            CellStyle tableHeader,
            CellStyle sectionTitle
    ) {
        Sheet summarySheet = wb.createSheet("Resumen");
        writeExcelSummary(summarySheet, 0, command, data);
        autosize(summarySheet, 2);

        Sheet alertsSheet = wb.createSheet("Alertas");
        writeExcelAlertsSection(alertsSheet, 0, data, tableHeader, sectionTitle);
        autosize(alertsSheet, ALERT_HEADERS.length);

        if (!data.readingStats().isEmpty()) {
            String telemetrySheetName =
                    command.type() == ReportType.REGULATORY_OEFA ? "Telemetria por nodo" : "Telemetria";
            Sheet telemetrySheet = wb.createSheet(telemetrySheetName);
            writeExcelTelemetrySection(telemetrySheet, 0, data, tableHeader, sectionTitle);
            autosize(telemetrySheet, TELEMETRY_HEADERS.length);
        }

        if (!data.rounds().isEmpty()) {
            Sheet roundsSheet = wb.createSheet("Rondas");
            writeExcelRoundsSection(roundsSheet, 0, data, tableHeader, sectionTitle);
            autosize(roundsSheet, ROUND_HEADERS.length);
        }
    }

    /** Tablas con cabecera de columnas (alertas siempre cuenta; telemetría y rondas si tienen filas). */
    private static int countExcelDataTables(ReportDataset data) {
        int count = 1;
        if (!data.readingStats().isEmpty()) {
            count++;
        }
        if (!data.rounds().isEmpty()) {
            count++;
        }
        return count;
    }

    private static int maxExcelColumns(ReportDataset data) {
        int max = ALERT_HEADERS.length;
        if (!data.readingStats().isEmpty()) {
            max = Math.max(max, TELEMETRY_HEADERS.length);
        }
        if (!data.rounds().isEmpty()) {
            max = Math.max(max, ROUND_HEADERS.length);
        }
        return max;
    }

    private static int writeExcelSummary(Sheet sheet, int row, GenerateReportCommand command, ReportDataset data) {
        row = summaryRow(sheet, row, "Reporte", typeLabel(command.type()));
        row = summaryRow(sheet, row, "Tranque", data.damLabel());
        row = summaryRow(sheet, row, "Desde", DAY.format(command.from()));
        row = summaryRow(sheet, row, "Hasta", DAY.format(command.to()));
        row = summaryRow(sheet, row, "Nodos en tranque", String.valueOf(data.nodes().size()));
        row = summaryRow(
                sheet,
                row,
                "Nodos con lecturas en rango",
                String.valueOf(data.readingStats().stream().filter(s -> s.count() > 0).count())
        );
        row = summaryRow(sheet, row, "Alertas listadas", String.valueOf(data.alerts().size()));
        row = summaryRow(sheet, row, "Rondas en rango", String.valueOf(data.rounds().size()));
        return summaryRow(sheet, row, "Generado", DT.format(OffsetDateTime.now()));
    }

    private static int writeExcelAlertsSection(
            Sheet sheet,
            int row,
            ReportDataset data,
            CellStyle tableHeader,
            CellStyle sectionTitle
    ) {
        row = sectionTitleRow(sheet, row, "Alertas", sectionTitle);
        if (data.alerts().isEmpty()) {
            return summaryRow(sheet, row, "Sin alertas", "No hay registros en el rango indicado.");
        }
        writeTableHeader(sheet, row++, ALERT_HEADERS, tableHeader);
        for (AlertLine alert : data.alerts()) {
            writeAlertRow(sheet.createRow(row++), alert);
        }
        return row;
    }

    private static int writeExcelTelemetrySection(
            Sheet sheet,
            int row,
            ReportDataset data,
            CellStyle tableHeader,
            CellStyle sectionTitle
    ) {
        row = sectionTitleRow(sheet, row, "Telemetria por nodo", sectionTitle);
        writeTableHeader(sheet, row++, TELEMETRY_HEADERS, tableHeader);
        for (ReadingStatsLine line : data.readingStats()) {
            writeTelemetryRow(sheet.createRow(row++), line);
        }
        return row;
    }

    private static int writeExcelRoundsSection(
            Sheet sheet,
            int row,
            ReportDataset data,
            CellStyle tableHeader,
            CellStyle sectionTitle
    ) {
        row = sectionTitleRow(sheet, row, "Rondas de inspeccion", sectionTitle);
        writeTableHeader(sheet, row++, ROUND_HEADERS, tableHeader);
        for (RoundLine round : data.rounds()) {
            writeRoundRow(sheet.createRow(row++), round);
        }
        return row;
    }

    private static int summaryRow(Sheet sheet, int index, String label, String value) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        return index + 1;
    }

    private static int blankRow(Sheet sheet, int index) {
        sheet.createRow(index);
        return index + 1;
    }

    private static int sectionTitleRow(Sheet sheet, int index, String title, CellStyle style) {
        Row row = sheet.createRow(index);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        return index + 1;
    }

    private static void writeTableHeader(Sheet sheet, int rowIndex, String[] headers, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < headers.length; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(headers[c]);
            cell.setCellStyle(style);
        }
    }

    private static void writeTelemetryRow(Row row, ReadingStatsLine line) {
        row.createCell(0).setCellValue(line.nodeLabel());
        row.createCell(1).setCellValue(line.sensorType());
        row.createCell(2).setCellValue(line.count());
        row.createCell(3).setCellValue(fmt(line.min()));
        row.createCell(4).setCellValue(fmt(line.max()));
        row.createCell(5).setCellValue(fmt(line.avg()));
        row.createCell(6).setCellValue(line.unit());
        row.createCell(7).setCellValue(line.lastStatus());
    }

    private static void writeAlertRow(Row row, AlertLine alert) {
        row.createCell(0).setCellValue(shortId(alert.id()));
        row.createCell(1).setCellValue(shortId(alert.nodeId()));
        row.createCell(2).setCellValue(alert.sensorType());
        row.createCell(3).setCellValue(alert.severity());
        row.createCell(4).setCellValue(fmt(alert.value()));
        row.createCell(5).setCellValue(alert.status());
    }

    private static void writeRoundRow(Row row, RoundLine round) {
        row.createCell(0).setCellValue(shortId(round.id()));
        row.createCell(1).setCellValue(round.scheduledAt() != null ? DT.format(round.scheduledAt()) : "—");
        row.createCell(2).setCellValue(round.completedAt() != null ? DT.format(round.completedAt()) : "—");
        row.createCell(3).setCellValue(round.status());
        row.createCell(4).setCellValue(shortId(round.operatorId()));
    }

    private static void autosize(Sheet sheet, int columns) {
        for (int c = 0; c < columns; c++) {
            sheet.autoSizeColumn(c);
            int width = sheet.getColumnWidth(c);
            sheet.setColumnWidth(c, Math.min(width + 512, 256 * 60));
        }
    }

    private static CellStyle tableHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle sectionTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private static String typeLabel(ReportType type) {
        return switch (type) {
            case REGULATORY_OEFA -> "Regulatorio OEFA";
            case ALERT_HISTORY -> "Historial de alertas";
            case INSPECTION_SUMMARY -> "Resumen de inspecciones";
        };
    }

    private static String fmt(BigDecimal value) {
        return value == null ? "—" : value.toPlainString();
    }

    private static String shortId(java.util.UUID id) {
        if (id == null) {
            return "—";
        }
        return id.toString().substring(0, 8);
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

    private static String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + ".";
    }

    private static final class PdfWriter {
        private static final float PAGE_W = PDRectangle.A4.getWidth();
        private static final float PAGE_H = PDRectangle.A4.getHeight();
        private static final float MARGIN = 42f;
        private static final float FOOTER_RESERVE = 48f;
        private static final float LINE_H = 12f;
        private static final float SECTION_GAP = 14f;
        private static final float SECTION_BAR_H = 20f;

        private static final float COL_NODE = MARGIN;
        private static final float COL_SENSOR = 198f;
        private static final float COL_N = 262f;
        private static final float COL_MIN = 288f;
        private static final float COL_MAX = 338f;
        private static final float COL_PROM = 388f;
        private static final float COL_UNIT = 438f;
        private static final float COL_STATUS = 478f;

        private static final float COL_ALERT_ID = MARGIN;
        private static final float COL_ALERT_NODE = 78f;
        private static final float COL_ALERT_SENSOR = 142f;
        private static final float COL_ALERT_SEV = 228f;
        private static final float COL_ALERT_VAL = 310f;
        private static final float COL_ALERT_ST = 388f;

        private static final float COL_ROUND_ID = MARGIN;
        private static final float COL_ROUND_SCHED = 78f;
        private static final float COL_ROUND_DONE = 198f;
        private static final float COL_ROUND_ST = 318f;
        private static final float COL_ROUND_OP = 398f;

        private final PDDocument doc;
        private PDPage page;
        private PDPageContentStream cs;
        private float y;

        private PdfWriter(PDDocument doc) throws IOException {
            this.doc = doc;
            openPage();
        }

        private void openPage() throws IOException {
            if (cs != null) {
                cs.close();
            }
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = PAGE_H - MARGIN;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < FOOTER_RESERVE) {
                openPage();
            }
        }

        private void drawReportHeader(GenerateReportCommand command, ReportDataset data) throws IOException {
            float barH = 52f;
            cs.setNonStrokingColor(255 / 255f, 140 / 255f, 66 / 255f);
            cs.addRect(0, PAGE_H - barH, PAGE_W, barH);
            cs.fill();
            textAt(PDType1Font.HELVETICA_BOLD, 18, MARGIN, PAGE_H - 30, "Sentinella — " + typeLabel(command.type()), 1f, 1f, 1f);
            textAt(
                    PDType1Font.HELVETICA,
                    10,
                    MARGIN,
                    PAGE_H - 44,
                    data.damLabel() + " | " + DAY.format(command.from()) + " a " + DAY.format(command.to()),
                    1f,
                    1f,
                    1f
            );
            y = PAGE_H - barH - SECTION_GAP;
        }

        private void drawSummary(ReportDataset data) throws IOException {
            sectionTitle("Resumen ejecutivo");
            paragraph("Nodos en tranque: " + data.nodes().size());
            long withData = data.readingStats().stream().filter(s -> s.count() > 0).count();
            paragraph("Nodos con lecturas en rango: " + withData);
            paragraph("Alertas listadas: " + data.alerts().size());
            paragraph("Rondas en rango: " + data.rounds().size());
            paragraph("Generado: " + DT.format(OffsetDateTime.now()));
            y -= 4;
        }

        private void sectionTitle(String title) throws IOException {
            y -= SECTION_GAP;
            ensureSpace(SECTION_BAR_H + SECTION_GAP);
            float barBottom = y - SECTION_BAR_H;
            cs.setNonStrokingColor(0.12f, 0.12f, 0.14f);
            cs.addRect(MARGIN, barBottom, PAGE_W - 2 * MARGIN, SECTION_BAR_H);
            cs.fill();
            textAt(PDType1Font.HELVETICA_BOLD, 11, MARGIN + 6, barBottom + 6, title, 1f, 1f, 1f);
            y = barBottom - 8;
        }

        private void drawTelemetryTable(List<ReadingStatsLine> lines) throws IOException {
            drawTelemetryHeader();
            for (ReadingStatsLine line : lines) {
                ensureSpace(LINE_H + 2);
                textAt(PDType1Font.HELVETICA, 8, COL_NODE, y, truncate(line.nodeLabel(), 34), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_SENSOR, y, truncate(line.sensorType(), 10), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_N, y, String.valueOf(line.count()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_MIN, y, fmt(line.min()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_MAX, y, fmt(line.max()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_PROM, y, fmt(line.avg()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_UNIT, y, truncate(line.unit(), 6), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_STATUS, y, truncate(line.lastStatus(), 10), 0.15f, 0.15f, 0.18f);
                y -= LINE_H;
            }
            y -= 4;
        }

        private void drawTelemetryHeader() throws IOException {
            ensureSpace(LINE_H + 4);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_NODE, y, "Nodo", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_SENSOR, y, "Sensor", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_N, y, "N", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_MIN, y, "Min", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_MAX, y, "Max", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_PROM, y, "Prom", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_UNIT, y, "Und", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_STATUS, y, "Estado", 0.1f, 0.1f, 0.12f);
            y -= LINE_H + 2;
        }

        private void drawAlertsTable(List<AlertLine> alerts) throws IOException {
            drawAlertsHeader();
            for (AlertLine alert : alerts) {
                ensureSpace(LINE_H + 2);
                textAt(PDType1Font.HELVETICA, 8, COL_ALERT_ID, y, shortId(alert.id()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_ALERT_NODE, y, shortId(alert.nodeId()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_ALERT_SENSOR, y, truncate(alert.sensorType(), 14), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_ALERT_SEV, y, truncate(alert.severity(), 12), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_ALERT_VAL, y, fmt(alert.value()), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_ALERT_ST, y, truncate(alert.status(), 12), 0.15f, 0.15f, 0.18f);
                y -= LINE_H;
            }
            y -= 4;
        }

        private void drawAlertsHeader() throws IOException {
            ensureSpace(LINE_H + 4);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ALERT_ID, y, "Id", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ALERT_NODE, y, "Nodo", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ALERT_SENSOR, y, "Sensor", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ALERT_SEV, y, "Severidad", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ALERT_VAL, y, "Valor", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ALERT_ST, y, "Estado", 0.1f, 0.1f, 0.12f);
            y -= LINE_H + 2;
        }

        private void drawRoundsTable(List<RoundLine> rounds) throws IOException {
            drawRoundsHeader();
            for (RoundLine round : rounds) {
                ensureSpace(LINE_H + 2);
                textAt(PDType1Font.HELVETICA, 8, COL_ROUND_ID, y, shortId(round.id()), 0.15f, 0.15f, 0.18f);
                textAt(
                        PDType1Font.HELVETICA,
                        8,
                        COL_ROUND_SCHED,
                        y,
                        round.scheduledAt() != null ? DT.format(round.scheduledAt()) : "-",
                        0.15f,
                        0.15f,
                        0.18f
                );
                textAt(
                        PDType1Font.HELVETICA,
                        8,
                        COL_ROUND_DONE,
                        y,
                        round.completedAt() != null ? DT.format(round.completedAt()) : "-",
                        0.15f,
                        0.15f,
                        0.18f
                );
                textAt(PDType1Font.HELVETICA, 8, COL_ROUND_ST, y, truncate(round.status(), 14), 0.15f, 0.15f, 0.18f);
                textAt(PDType1Font.HELVETICA, 8, COL_ROUND_OP, y, shortId(round.operatorId()), 0.15f, 0.15f, 0.18f);
                y -= LINE_H;
            }
        }

        private void drawRoundsHeader() throws IOException {
            ensureSpace(LINE_H + 4);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ROUND_ID, y, "Id", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ROUND_SCHED, y, "Programada", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ROUND_DONE, y, "Completada", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ROUND_ST, y, "Estado", 0.1f, 0.1f, 0.12f);
            textAt(PDType1Font.HELVETICA_BOLD, 8, COL_ROUND_OP, y, "Operador", 0.1f, 0.1f, 0.12f);
            y -= LINE_H + 2;
        }

        private void paragraph(String text) throws IOException {
            ensureSpace(LINE_H);
            textAt(PDType1Font.HELVETICA, 9, MARGIN, y, text, 0.15f, 0.15f, 0.18f);
            y -= LINE_H;
        }

        private void textAt(
                PDType1Font font,
                float size,
                float x,
                float baseline,
                String text,
                float r,
                float g,
                float b
        ) throws IOException {
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(r, g, b);
            cs.newLineAtOffset(x, baseline);
            cs.showText(pdfSafe(text));
            cs.endText();
        }

        private void finish() throws IOException {
            if (cs != null) {
                textAt(PDType1Font.HELVETICA, 8, MARGIN, 28, "ChicamaX Sentinella — documento confidencial", 0.45f, 0.45f, 0.5f);
                cs.close();
                cs = null;
            }
        }
    }
}
