package uz.agrobank.stopcredit.service;

import uz.agrobank.stopcredit.exception.ApiException;

final class StaleCheck {

    private StaleCheck() {
    }

    // Clients that send the version they loaded get protection against silently overwriting someone else's edit
    static void requireCurrent(Long expectedVersion, long actualVersion) {
        if (expectedVersion != null && expectedVersion != actualVersion) {
            throw ApiException.conflict(
                    "Yozuvni boshqa foydalanuvchi o'zgartirgan. Sahifani yangilab, qayta urinib ko'ring");
        }
    }
}
