package uz.agrobank.stopcredit.component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.dto.CreditResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CreditExcelExporter {

    private static final String[] HEADERS = {
            "ID", "Application No", "Last name", "First name", "Middle name", "PINFL", "Type", "MFO",
            "Amount (UZS)", "Status", "Stage", "Deadline", "Danger", "Created by", "Created at", "Updated at"};

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Tashkent"));

    public byte[] export(List<CreditResponse> credits) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Credits");

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
                sheet.setColumnWidth(i, 20 * 256);
            }
            sheet.createFreezePane(0, 1);

            int rowIndex = 1;
            for (CreditResponse c : credits) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(c.id());
                row.createCell(1).setCellValue(c.applicationNumber());
                row.createCell(2).setCellValue(c.lastName());
                row.createCell(3).setCellValue(c.firstName());
                row.createCell(4).setCellValue(c.middleName() == null ? "" : c.middleName());
                row.createCell(5).setCellValue(c.pinfl());
                row.createCell(6).setCellValue(c.type().name());
                row.createCell(7).setCellValue(c.mfo());
                Cell amount = row.createCell(8);
                amount.setCellValue(c.amount().doubleValue());
                amount.setCellStyle(moneyStyle);
                row.createCell(9).setCellValue(c.status().name());
                row.createCell(10).setCellValue(c.stage().name());
                row.createCell(11).setCellValue(format(c.stageDeadline()));
                row.createCell(12).setCellValue(c.danger() ? "YES" : "");
                row.createCell(13).setCellValue(c.createdBy());
                row.createCell(14).setCellValue(format(c.createdAt()));
                row.createCell(15).setCellValue(format(c.updatedAt()));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String format(Instant instant) {
        return instant == null ? "" : DATE_TIME.format(instant);
    }
}
