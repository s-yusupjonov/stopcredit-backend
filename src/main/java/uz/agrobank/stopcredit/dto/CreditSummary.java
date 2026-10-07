package uz.agrobank.stopcredit.dto;

public record CreditSummary(long total, long overdue, long ownStage, long completed) {
}
