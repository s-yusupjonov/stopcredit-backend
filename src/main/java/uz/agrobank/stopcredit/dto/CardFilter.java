package uz.agrobank.stopcredit.dto;

import org.springframework.format.annotation.DateTimeFormat;
import uz.agrobank.stopcredit.domain.CardBasisCategory;
import uz.agrobank.stopcredit.domain.CardRestrictionType;
import uz.agrobank.stopcredit.domain.CardStatus;

import java.time.LocalDate;

public record CardFilter(
        String q,
        CardStatus status,
        String mfo,
        CardRestrictionType restrictionType,
        CardBasisCategory basisCategory,
        Long executorId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
}
