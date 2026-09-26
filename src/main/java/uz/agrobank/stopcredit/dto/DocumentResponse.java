package uz.agrobank.stopcredit.dto;

import uz.agrobank.stopcredit.domain.CreditStage;

import java.time.Instant;

public record DocumentResponse(Long id, CreditStage stage, String fileName, long sizeBytes,
                               String uploadedBy, Instant uploadedAt) {
}
