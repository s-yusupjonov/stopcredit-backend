package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uz.agrobank.stopcredit.domain.CardBasisCategory;
import uz.agrobank.stopcredit.domain.CardRestrictionType;
import uz.agrobank.stopcredit.domain.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardRequest(
        @NotBlank @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits") String cardNumber,
        @Size(max = 10) String mfo,
        LocalDate restrictionDate,
        @Digits(integer = 17, fraction = 2) BigDecimal balance,
        CardRestrictionType restrictionType,
        @NotNull CardBasisCategory basisCategory,
        @Size(max = 500) String basisComment,
        @NotNull CardStatus status,
        @Size(max = 500) String statusComment,
        @NotNull Long executorId) {
}
