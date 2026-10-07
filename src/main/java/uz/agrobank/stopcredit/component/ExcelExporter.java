package uz.agrobank.stopcredit.component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;

public abstract class ExcelExporter<T> {

    private static final int IN_MEMORY_ROWS = 200;
    private static final int MAX_NUMERIC_PRECISION = 15;
    private static final int CHARACTER_WIDTH_UNITS = 256;

    protected abstract String sheetName();

    protected abstract String[] headers();

    protected abstract int columnWidth();

    protected abstract void fillRow(Row row, T item, CellStyle moneyStyle);

    public byte[] export(List<T> items) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(IN_MEMORY_ROWS);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName());
            writeHeader(workbook, sheet);
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            int rowIndex = 1;
            for (T item : items) {
                fillRow(sheet.createRow(rowIndex++), item, moneyStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static String text(String value) {
        return value == null ? "" : value;
    }

    // Excel numbers hold ~15 significant digits; larger amounts are written as text so no digit is lost
    protected static void setMoney(Cell cell, BigDecimal value, CellStyle moneyStyle) {
        if (value.precision() <= MAX_NUMERIC_PRECISION) {
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(moneyStyle);
        } else {
            cell.setCellValue(value.toPlainString());
        }
    }

    private void writeHeader(SXSSFWorkbook workbook, Sheet sheet) {
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(bold);

        String[] headers = headers();
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, columnWidth() * CHARACTER_WIDTH_UNITS);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
    }
}
