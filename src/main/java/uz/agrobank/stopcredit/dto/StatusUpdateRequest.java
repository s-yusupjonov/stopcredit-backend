package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.NotNull;
import uz.agrobank.stopcredit.domain.CreditStatus;

public record StatusUpdateRequest(@NotNull CreditStatus status) {
}
