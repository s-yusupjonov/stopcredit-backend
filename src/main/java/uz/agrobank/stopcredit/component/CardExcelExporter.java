package uz.agrobank.stopcredit.component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.dto.CardResponse;
import uz.agrobank.stopcredit.dto.ExecutorResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class CardExcelExporter {

    private static final String[] HEADERS = {
            "T/r", "Karta raqami", "MFO", "Sana", "Karta balansi", "Cheklov turi", "Asos",
            "Buyruq raqami", "Status", "Eslatma", "Ijrochi", "Yuboruvchi", "Yaratilgan",
            "Ochish buyruq raqami", "Ochish eslatmasi", "Ochilgan sana"};

    private static final Map<String, String> STATUS = Map.of("ACTIVE", "Aktiv", "BLOCKED", "Bloklangan");
    private static final Map<String, String> RESTRICTION = Map.of("FULL", "To'liq", "PARTIAL", "Qisman");
    private static final Map<String, String> BASIS = Map.of(
            "CENTRAL_BANK", "Markaziy Bank",
            "INTERNAL_AFFAIRS", "Ichki Ishlar Vazirligi",
            "OTHER", "Boshqa");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("Asia/Tashkent"));

    public byte[] export(List<CardResponse> cards) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Kartalar");

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(bold);
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 22 * 256);
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            int rowIndex = 1;
            for (CardResponse c : cards) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(c.id());
                row.createCell(1).setCellValue(c.cardNumber());
                row.createCell(2).setCellValue(text(c.mfo()));
                row.createCell(3).setCellValue(format(c.restrictionDate()));
                if (c.balance() != null) {
                    Cell balance = row.createCell(4);
                    balance.setCellValue(c.balance().doubleValue());
                    balance.setCellStyle(moneyStyle);
                }
                row.createCell(5).setCellValue(c.restrictionType() == null ? "" : RESTRICTION.get(c.restrictionType().name()));
                row.createCell(6).setCellValue(BASIS.get(c.basisCategory().name()));
                row.createCell(7).setCellValue(text(c.basisComment()));
                row.createCell(8).setCellValue(STATUS.get(c.status().name()));
                row.createCell(9).setCellValue(text(c.statusComment()));
                row.createCell(10).setCellValue(executor(c.executor()));
                row.createCell(11).setCellValue(c.senderName());
                row.createCell(12).setCellValue(format(c.createdAt()));
                row.createCell(13).setCellValue(text(c.unblockOrderNumber()));
                row.createCell(14).setCellValue(text(c.unblockComment()));
                row.createCell(15).setCellValue(format(c.unblockedAt()));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String executor(ExecutorResponse executor) {
        StringBuilder label = new StringBuilder(executor.name());
        if (!executor.phone().isEmpty()) {
            label.append(' ').append(executor.phone());
        }
        if (!executor.extension().isEmpty()) {
            label.append(" (").append(executor.extension()).append(')');
        }
        return label.toString();
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private String format(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    private String format(Instant instant) {
        return instant == null ? "" : DATE_TIME.format(instant);
    }
}