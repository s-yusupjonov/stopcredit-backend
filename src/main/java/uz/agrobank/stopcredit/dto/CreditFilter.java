package uz.agrobank.stopcredit.dto;

import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.domain.CreditType;

public record CreditFilter(String q, CreditStatus status, CreditType type, CreditStage stage, Boolean danger) {
}
