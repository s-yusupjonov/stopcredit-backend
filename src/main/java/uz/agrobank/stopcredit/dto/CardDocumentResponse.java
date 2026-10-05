package uz.agrobank.stopcredit.dto;

import uz.agrobank.stopcredit.domain.CardDocumentKind;

import java.time.Instant;

public record CardDocumentResponse(Long id, String fileName, long sizeBytes, CardDocumentKind kind,
                                   String uploadedBy, Instant uploadedAt) {
}