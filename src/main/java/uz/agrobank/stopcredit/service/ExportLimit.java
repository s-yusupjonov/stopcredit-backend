package uz.agrobank.stopcredit.service;

import uz.agrobank.stopcredit.exception.ApiException;

final class ExportLimit {

    static final int MAX_ROWS = 50_000;

    private ExportLimit() {
    }

    static void require(long rows) {
        if (rows > MAX_ROWS) {
            throw ApiException.badRequest(
                    "Export is limited to %d rows, found %d. Narrow the filters".formatted(MAX_ROWS, rows));
        }
    }
}
