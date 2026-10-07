package uz.agrobank.stopcredit.mapper;

import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.domain.Card;
import uz.agrobank.stopcredit.domain.CardDocument;
import uz.agrobank.stopcredit.domain.Executor;
import uz.agrobank.stopcredit.dto.CardDocumentResponse;
import uz.agrobank.stopcredit.dto.CardRequest;
import uz.agrobank.stopcredit.dto.CardResponse;
import uz.agrobank.stopcredit.dto.ExecutorRequest;
import uz.agrobank.stopcredit.dto.ExecutorResponse;

import java.util.List;

@Component
public class CardMapper {

    public void apply(Card card, CardRequest request, Executor executor) {
        card.setCardNumber(request.cardNumber().trim());
        card.setMfo(blankToNull(request.mfo()));
        card.setRestrictionDate(request.restrictionDate());
        card.setBalance(request.balance());
        card.setRestrictionType(request.restrictionType());
        card.setBasisCategory(request.basisCategory());
        card.setBasisComment(blankToNull(request.basisComment()));
        card.setStatus(request.status());
        card.setStatusComment(blankToNull(request.statusComment()));
        card.setExecutor(executor);
    }

    public void apply(Executor executor, ExecutorRequest request) {
        executor.setName(request.name().trim());
        executor.setPhone(blankToEmpty(request.phone()));
        executor.setExtension(blankToEmpty(request.extension()));
    }

    public CardResponse toResponse(Card card, List<CardDocumentResponse> documents) {
        return new CardResponse(
                card.getId(),
                card.getCardNumber(),
                card.getMfo(),
                card.getRestrictionDate(),
                card.getBalance(),
                card.getRestrictionType(),
                card.getBasisCategory(),
                card.getBasisComment(),
                card.getStatus(),
                card.getStatusComment(),
                toResponse(card.getExecutor()),
                card.getSenderName(),
                card.getCreatedAt(),
                card.getUpdatedAt(),
                card.getUnblockOrderNumber(),
                card.getUnblockComment(),
                card.getUnblockedAt(),
                card.getUnblockedBy(),
                documents);
    }

    public ExecutorResponse toResponse(Executor executor) {
        return new ExecutorResponse(executor.getId(), executor.getName(), executor.getPhone(), executor.getExtension());
    }

    public CardDocumentResponse toResponse(CardDocument document) {
        return new CardDocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getSizeBytes(),
                document.getKind(),
                document.getUploadedBy(),
                document.getUploadedAt());
    }

    public String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}