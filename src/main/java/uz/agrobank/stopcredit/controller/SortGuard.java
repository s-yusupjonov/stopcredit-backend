package uz.agrobank.stopcredit.controller;

import org.springframework.data.domain.Pageable;
import uz.agrobank.stopcredit.exception.ApiException;

import java.util.Set;

final class SortGuard {

    static final Set<String> CREDIT_PROPERTIES = Set.of(
            "id", "createdAt", "updatedAt", "amount", "stageDeadline", "applicationNumber",
            "lastName", "firstName", "status", "stage", "type", "mfo");

    static final Set<String> CARD_PROPERTIES = Set.of(
            "id", "createdAt", "updatedAt", "restrictionDate", "balance", "cardNumber", "mfo", "status");

    private SortGuard() {
    }

    // Without a whitelist a client could sort by nested fields such as createdBy.passwordHash
    static Pageable allowOnly(Pageable pageable, Set<String> allowed) {
        pageable.getSort().forEach(order -> {
            if (!allowed.contains(order.getProperty())) {
                throw ApiException.badRequest("Bu maydon bo'yicha saralab bo'lmaydi: " + order.getProperty());
            }
        });
        return pageable;
    }
}
