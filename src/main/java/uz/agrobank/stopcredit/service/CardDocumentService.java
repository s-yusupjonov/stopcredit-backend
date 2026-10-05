package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.agrobank.stopcredit.domain.Card;
import uz.agrobank.stopcredit.domain.CardDocument;
import uz.agrobank.stopcredit.domain.CardDocumentKind;
import uz.agrobank.stopcredit.dto.CardDocumentResponse;
import uz.agrobank.stopcredit.dto.DownloadedFile;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CardMapper;
import uz.agrobank.stopcredit.repository.CardDocumentRepository;
import uz.agrobank.stopcredit.repository.CardRepository;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardDocumentService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final CardDocumentRepository documentRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper mapper;
    private final FileStorage storage;

    @Transactional
    public List<CardDocumentResponse> upload(AuthUser user, Long cardId, List<MultipartFile> files) {
        Card card = findCard(cardId);
        String uploader = userRepository.findById(user.id())
                .orElseThrow(() -> ApiException.unauthorized("User not found"))
                .getFullName();
        return attach(card, uploader, files, CardDocumentKind.RESTRICTION);
    }

    @Transactional
    public List<CardDocumentResponse> attach(Card card, String uploader, List<MultipartFile> files,
                                             CardDocumentKind kind) {
        if (files == null || files.isEmpty()) {
            throw ApiException.badRequest("At least one PDF file is required");
        }
        files.forEach(this::requirePdf);
        return files.stream()
                .map(file -> mapper.toResponse(store(card, uploader, file, kind)))
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadedFile download(Long cardId, Long documentId) {
        CardDocument document = find(cardId, documentId);
        return new DownloadedFile(document.getFileName(), document.getSizeBytes(),
                storage.get(document.getObjectKey()));
    }

    @Transactional
    public void delete(Long cardId, Long documentId) {
        CardDocument document = find(cardId, documentId);
        storage.delete(document.getObjectKey());
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public List<CardDocumentResponse> findByCard(Long cardId) {
        return documentRepository.findByCardIdOrderByUploadedAtAsc(cardId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private CardDocument store(Card card, String uploader, MultipartFile file, CardDocumentKind kind) {
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
        document.setKind(kind);
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
}