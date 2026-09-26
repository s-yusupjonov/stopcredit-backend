package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.domain.CreditType;

import java.math.BigDecimal;

public record CreditRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 100) String middleName,
        @NotBlank @Pattern(regexp = "\\d{14}", message = "PINFL must be exactly 14 digits") String pinfl,
        @NotNull CreditType type,
        @NotBlank @Size(max = 25) String mfo,
        @NotBlank @Size(max = 25) String applicationNumber,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotNull CreditStatus status) {
}
