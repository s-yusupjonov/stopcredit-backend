package uz.agrobank.stopcredit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import uz.agrobank.stopcredit.domain.CardBasisCategory;
import uz.agrobank.stopcredit.domain.CardRestrictionType;
import uz.agrobank.stopcredit.domain.CardStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CardResponse(
        Long id,
        String cardNumber,
        String mfo,
        LocalDate restrictionDate,
        @JsonInclude(JsonInclude.Include.ALWAYS) BigDecimal balance,
        CardRestrictionType restrictionType,
        CardBasisCategory basisCategory,
        String basisComment,
        CardStatus status,
        String statusComment,
        ExecutorResponse executor,
        String senderName,
        Instant createdAt,
        Instant updatedAt,
        String unblockOrderNumber,
        String unblockComment,
        Instant unblockedAt,
        String unblockedBy,
        List<CardDocumentResponse> documents) {
}