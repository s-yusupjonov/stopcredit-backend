package uz.agrobank.stopcredit.dto;

import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.domain.CreditType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreditResponse(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        String pinfl,
        CreditType type,
        String mfo,
        String applicationNumber,
        BigDecimal amount,
        CreditStatus status,
        CreditStage stage,
        Instant stageDeadline,
        boolean danger,
        String createdBy,
        Instant createdAt,
        Instant updatedAt,
        long version,
        List<DocumentResponse> documents) {
}
