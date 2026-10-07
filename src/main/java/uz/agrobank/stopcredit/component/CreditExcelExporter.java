package uz.agrobank.stopcredit.component;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.domain.CreditType;
import uz.agrobank.stopcredit.dto.CreditResponse;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class CreditExcelExporter extends ExcelExporter<CreditResponse> {

    private static final String[] HEADERS = {
            "ID", "Ariza raqami", "Familiya", "Ism", "Sharif", "PINFL", "Kredit turi", "MFO",
            "Summa (so'm)", "Status", "Bosqich", "Muddat", "Muddati o'tgan", "Yaratdi", "Yaratilgan", "Yangilangan"};

    // same wording as the web UI so the spreadsheet reads like the screen it was exported from
    private static final Map<CreditType, String> TYPE = Map.of(
            CreditType.ONLINE, "Onlayn",
            CreditType.CHAKANA, "Chakana",
            CreditType.OPEN, "Ochiq");
    private static final Map<CreditStatus, String> STATUS = Map.of(
            CreditStatus.ACTIVE, "Faol",
            CreditStatus.STOPPED, "To'xtatilgan");
    private static final Map<CreditStage, String> STAGE = Map.of(
            CreditStage.ANTI_FRAUD, "Anti-fraud",
            CreditStage.CREDIT_MANAGEMENT, "Kredit boshqaruvi",
            CreditStage.LEGAL, "Yuridik bo'lim",
            CreditStage.UNDERWRITING, "Muammoli kreditlar",
            CreditStage.COMPLETED, "Yakunlangan");

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("Asia/Tashkent"));

    @Override
    protected String sheetName() {
        return "Kreditlar";
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
        row.createCell(6).setCellValue(TYPE.get(c.type()));
        row.createCell(7).setCellValue(c.mfo());
        setMoney(row.createCell(8), c.amount(), moneyStyle);
        row.createCell(9).setCellValue(STATUS.get(c.status()));
        row.createCell(10).setCellValue(STAGE.get(c.stage()));
        row.createCell(11).setCellValue(format(c.stageDeadline()));
        row.createCell(12).setCellValue(c.danger() ? "Ha" : "");
        row.createCell(13).setCellValue(c.createdBy());
        row.createCell(14).setCellValue(format(c.createdAt()));
        row.createCell(15).setCellValue(format(c.updatedAt()));
    }

    private String format(Instant instant) {
        return instant == null ? "" : DATE_TIME.format(instant);
    }
}
