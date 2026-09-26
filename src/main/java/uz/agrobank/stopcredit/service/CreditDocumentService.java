package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
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
public class CreditDocumentService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final CreditDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CreditAccess access;
    private final CreditMapper mapper;
    private final FileStorage storage;

    @Transactional
    public List<DocumentResponse> upload(AuthUser user, Long creditId, List<MultipartFile> files) {
        Credit credit = access.loadForWork(user, creditId);
        if (files == null || files.isEmpty()) {
            throw ApiException.badRequest("At least one PDF file is required");
        }
        files.forEach(this::requirePdf); // validate all before storing any
        User uploader = userRepository.getReferenceById(user.id());
        return files.stream()
                .map(file -> mapper.toResponse(store(credit, uploader, file)))
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
            throw ApiException.forbidden("Documents of previous stages cannot be removed");
        }
        storage.delete(document.getObjectKey());
        documentRepository.delete(document);
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

    private CreditDocument store(Credit credit, User uploader, MultipartFile file) {
        String objectKey = "credits/%d/%s/%s.pdf".formatted(
                credit.getId(), credit.getStage().name().toLowerCase(Locale.ROOT), UUID.randomUUID());
        try (InputStream in = file.getInputStream()) {
            storage.put(objectKey, in, file.getSize(), PDF_CONTENT_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        CreditDocument document = new CreditDocument();
        document.setCredit(credit);
        document.setStage(credit.getStage());
        document.setFileName(StringUtils.getFilename(StringUtils.cleanPath(file.getOriginalFilename())));
        document.setObjectKey(objectKey);
        document.setSizeBytes(file.getSize());
        document.setUploadedBy(uploader);
        return documentRepository.saveAndFlush(document);
    }

    private CreditDocument find(Credit credit, Long documentId) {
        return documentRepository.findByIdAndCreditId(documentId, credit.getId())
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
