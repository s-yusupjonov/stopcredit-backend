package uz.agrobank.stopcredit.dto;

import java.time.Instant;

public record CardDocumentResponse(Long id, String fileName, long sizeBytes, String uploadedBy, Instant uploadedAt) {
}
