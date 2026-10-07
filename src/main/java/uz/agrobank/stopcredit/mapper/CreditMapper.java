package uz.agrobank.stopcredit.mapper;

import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditDocument;
import uz.agrobank.stopcredit.dto.CreditRequest;
import uz.agrobank.stopcredit.dto.CreditResponse;
import uz.agrobank.stopcredit.dto.DocumentResponse;

import java.time.Instant;
import java.util.List;

@Component
public class CreditMapper {

    public void apply(Credit credit, CreditRequest request) {
        credit.setFirstName(request.firstName().trim());
        credit.setLastName(request.lastName().trim());
        credit.setMiddleName(request.middleName() == null || request.middleName().isBlank()
                ? null : request.middleName().trim());
        credit.setPinfl(request.pinfl());
        credit.setType(request.type());
        credit.setMfo(request.mfo().trim());
        credit.setApplicationNumber(request.applicationNumber().trim());
        credit.setAmount(request.amount());
    }

    public CreditResponse toResponse(Credit credit, Instant now, List<DocumentResponse> documents) {
        boolean danger = credit.getStageDeadline() != null && now.isAfter(credit.getStageDeadline());
        return new CreditResponse(
                credit.getId(),
                credit.getFirstName(),
                credit.getLastName(),
                credit.getMiddleName(),
                credit.getPinfl(),
                credit.getType(),
                credit.getMfo(),
                credit.getApplicationNumber(),
                credit.getAmount(),
                credit.getStatus(),
                credit.getStage(),
                credit.getStageDeadline(),
                danger,
                credit.getCreatedBy().getFullName(),
                credit.getCreatedAt(),
                credit.getUpdatedAt(),
                documents);
    }

    public DocumentResponse toResponse(CreditDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getStage(),
                document.getFileName(),
                document.getSizeBytes(),
                document.getUploadedBy().getFullName(),
                document.getUploadedAt());
    }
}
