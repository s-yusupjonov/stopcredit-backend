package uz.agrobank.stopcredit.component;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.dto.CreditResponse;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class CreditExcelExporter extends ExcelExporter<CreditResponse> {

    private static final String[] HEADERS = {
            "ID", "Application No", "Last name", "First name", "Middle name", "PINFL", "Type", "MFO",
            "Amount (UZS)", "Status", "Stage", "Deadline", "Danger", "Created by", "Created at", "Updated at"};

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Tashkent"));

    @Override
    protected String sheetName() {
        return "Credits";
    }

    @Override
    protected String[] headers() {
        return HEADERS;
    }

    @Override
    protected int columnWidth() {
        return 20;
    }

    @Override
    protected void fillRow(Row row, CreditResponse c, CellStyle moneyStyle) {
        row.createCell(0).setCellValue(c.id());
        row.createCell(1).setCellValue(c.applicationNumber());
        row.createCell(2).setCellValue(c.lastName());
        row.createCell(3).setCellValue(c.firstName());
        row.createCell(4).setCellValue(text(c.middleName()));
        row.createCell(5).setCellValue(c.pinfl());
        row.createCell(6).setCellValue(c.type().name());
        row.createCell(7).setCellValue(c.mfo());
        setMoney(row.createCell(8), c.amount(), moneyStyle);
        row.createCell(9).setCellValue(c.status().name());
        row.createCell(10).setCellValue(c.stage().name());
        row.createCell(11).setCellValue(format(c.stageDeadline()));
        row.createCell(12).setCellValue(c.danger() ? "YES" : "");
        row.createCell(13).setCellValue(c.createdBy());
        row.createCell(14).setCellValue(format(c.createdAt()));
        row.createCell(15).setCellValue(format(c.updatedAt()));
    }

    private String format(Instant instant) {
        return instant == null ? "" : DATE_TIME.format(instant);
    }
}
