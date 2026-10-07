package uz.agrobank.stopcredit.component;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.dto.CardResponse;
import uz.agrobank.stopcredit.dto.ExecutorResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class CardExcelExporter extends ExcelExporter<CardResponse> {

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

    @Override
    protected String sheetName() {
        return "Kartalar";
    }

    @Override
    protected String[] headers() {
        return HEADERS;
    }

    @Override
    protected int columnWidth() {
        return 22;
    }

    @Override
    protected void fillRow(Row row, CardResponse c, CellStyle moneyStyle) {
        row.createCell(0).setCellValue(c.id());
        row.createCell(1).setCellValue(c.cardNumber());
        row.createCell(2).setCellValue(text(c.mfo()));
        row.createCell(3).setCellValue(format(c.restrictionDate()));
        if (c.balance() != null) {
            setMoney(row.createCell(4), c.balance(), moneyStyle);
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

    private String format(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    private String format(Instant instant) {
        return instant == null ? "" : DATE_TIME.format(instant);
    }
}