package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.agrobank.stopcredit.domain.AuditAction;
import uz.agrobank.stopcredit.domain.AuditEntity;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditDocument;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.DocumentResponse;
import uz.agrobank.stopcredit.dto.DownloadedFile;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CreditMapper;
import uz.agrobank.stopcredit.repository.CreditDocumentRepository;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CreditDocumentService {

    private final CreditDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CreditAccess access;
    private final CreditMapper mapper;
    private final FileStorage storage;
    private final PdfStorage pdfStorage;
    private final AuditService audit;

    @Transactional
    public List<DocumentResponse> upload(AuthUser user, Long creditId, List<MultipartFile> files) {
        Credit credit = access.loadForWork(user, creditId);
        User uploader = userRepository.getReferenceById(user.id());
        String keyPrefix = "credits/%d/%s/".formatted(credit.getId(), credit.getStage().name().toLowerCase(Locale.ROOT));
        return pdfStorage.storeAll(files, keyPrefix).stream()
                .map(pdf -> mapper.toResponse(save(user, credit, uploader, pdf)))
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadedFile download(AuthUser user, Long creditId, Long documentId) {
        Credit credit = access.loadVisible(user, creditId);
        CreditDocument document = find(credit, documentId);
        return new DownloadedFile(document.getFileName(), document.getSizeBytes(),
                storage.get(document.getObjectKey()));
    }

    @Transactional
    public void delete(AuthUser user, Long creditId, Long documentId) {
        Credit credit = access.loadForWork(user, creditId);
        CreditDocument document = find(credit, documentId);
        if (document.getStage() != credit.getStage()) {
            throw ApiException.forbidden("Oldingi bosqich hujjatlarini o'chirib bo'lmaydi");
        }
        documentRepository.delete(document);
        pdfStorage.deleteAfterCommit(document.getObjectKey());
        audit.record(user, AuditEntity.CREDIT, creditId, AuditAction.DELETE_DOCUMENT, document.getFileName());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findByCredit(Long creditId) {
        return documentRepository.findByCreditIdOrderByUploadedAtAsc(creditId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsForStage(Long creditId, CreditStage stage) {
        return documentRepository.existsByCreditIdAndStage(creditId, stage);
    }

    private CreditDocument save(AuthUser actor, Credit credit, User uploader, StoredPdf pdf) {
        CreditDocument document = new CreditDocument();
        document.setCredit(credit);
        document.setStage(credit.getStage());
        document.setFileName(pdf.fileName());
        document.setObjectKey(pdf.objectKey());
        document.setSizeBytes(pdf.sizeBytes());
        document.setUploadedBy(uploader);
        CreditDocument saved = documentRepository.saveAndFlush(document);
        audit.record(actor, AuditEntity.CREDIT, credit.getId(), AuditAction.UPLOAD_DOCUMENT, pdf.fileName());
        return saved;
    }

    private CreditDocument find(Credit credit, Long documentId) {
        return documentRepository.findByIdAndCreditId(documentId, credit.getId())
                .orElseThrow(() -> ApiException.notFound("Hujjat topilmadi: " + documentId));
    }
}
