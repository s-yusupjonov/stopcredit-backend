package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.agrobank.stopcredit.domain.AuditAction;
import uz.agrobank.stopcredit.domain.AuditEntity;
import uz.agrobank.stopcredit.domain.Card;
import uz.agrobank.stopcredit.domain.CardDocument;
import uz.agrobank.stopcredit.dto.CardDocumentResponse;
import uz.agrobank.stopcredit.dto.DownloadedFile;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CardMapper;
import uz.agrobank.stopcredit.repository.CardDocumentRepository;
import uz.agrobank.stopcredit.repository.CardRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardDocumentService {

    private final CardDocumentRepository documentRepository;
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardMapper mapper;
    private final FileStorage storage;
    private final PdfStorage pdfStorage;
    private final AuditService audit;

    @Transactional
    public List<CardDocumentResponse> upload(AuthUser user, Long cardId, List<MultipartFile> files) {
        Card card = findCard(cardId);
<<<<<<< Updated upstream
        if (files == null || files.isEmpty()) {
            throw ApiException.badRequest("At least one PDF file is required");
        }
        files.forEach(this::requirePdf);
        String uploader = userRepository.findById(user.id())
                .orElseThrow(() -> ApiException.unauthorized("User not found"))
                .getFullName();
        return files.stream()
                .map(file -> mapper.toResponse(store(card, uploader, file)))
=======
        List<CardDocumentResponse> uploaded = attach(card, userService.fullNameOf(user), files, CardDocumentKind.RESTRICTION);
        uploaded.forEach(document -> audit.record(user, AuditEntity.CARD, cardId,
                AuditAction.UPLOAD_DOCUMENT, document.fileName()));
        return uploaded;
    }

    @Transactional
    public List<CardDocumentResponse> attach(Card card, String uploader, List<MultipartFile> files,
                                             CardDocumentKind kind) {
        return pdfStorage.storeAll(files, "cards/%d/".formatted(card.getId())).stream()
                .map(pdf -> mapper.toResponse(save(card, uploader, pdf, kind)))
>>>>>>> Stashed changes
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadedFile download(Long cardId, Long documentId) {
        CardDocument document = find(cardId, documentId);
        return new DownloadedFile(document.getFileName(), document.getSizeBytes(),
                storage.get(document.getObjectKey()));
    }

    @Transactional
    public void delete(AuthUser user, Long cardId, Long documentId) {
        CardDocument document = find(cardId, documentId);
        documentRepository.delete(document);
        pdfStorage.deleteAfterCommit(document.getObjectKey());
        audit.record(user, AuditEntity.CARD, cardId, AuditAction.DELETE_DOCUMENT, document.getFileName());
    }

    @Transactional(readOnly = true)
    public List<CardDocumentResponse> findByCard(Long cardId) {
        return documentRepository.findByCardIdOrderByUploadedAtAsc(cardId).stream()
                .map(mapper::toResponse)
                .toList();
    }

<<<<<<< Updated upstream
    private CardDocument store(Card card, String uploader, MultipartFile file) {
        String objectKey = "cards/%d/%s.pdf".formatted(card.getId(), UUID.randomUUID());
        try (InputStream in = file.getInputStream()) {
            storage.put(objectKey, in, file.getSize(), PDF_CONTENT_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        CardDocument document = new CardDocument();
        document.setCard(card);
        document.setFileName(StringUtils.getFilename(StringUtils.cleanPath(file.getOriginalFilename())));
        document.setObjectKey(objectKey);
        document.setSizeBytes(file.getSize());
=======
    private CardDocument save(Card card, String uploader, StoredPdf pdf, CardDocumentKind kind) {
        CardDocument document = new CardDocument();
        document.setCard(card);
        document.setFileName(pdf.fileName());
        document.setObjectKey(pdf.objectKey());
        document.setSizeBytes(pdf.sizeBytes());
        document.setKind(kind);
>>>>>>> Stashed changes
        document.setUploadedBy(uploader);
        return documentRepository.saveAndFlush(document);
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> ApiException.notFound("Card not found: " + cardId));
    }

    private CardDocument find(Long cardId, Long documentId) {
        return documentRepository.findByIdAndCardId(documentId, cardId)
                .orElseThrow(() -> ApiException.notFound("Document not found: " + documentId));
    }
<<<<<<< Updated upstream

    private void requirePdf(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (file.isEmpty() || name == null || !name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw ApiException.badRequest("Only non-empty PDF files are allowed: " + name);
        }
        try (InputStream in = file.getInputStream()) {
            if (!Arrays.equals(in.readNBytes(PDF_MAGIC.length), PDF_MAGIC)) {
                throw ApiException.badRequest("File is not a valid PDF: " + name);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
=======
>>>>>>> Stashed changes
}
