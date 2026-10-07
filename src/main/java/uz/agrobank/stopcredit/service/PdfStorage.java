package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfStorage {

    private static final String CONTENT_TYPE = "application/pdf";
    private static final byte[] HEADER = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final String TRAILER = "%%EOF";
    private static final int TRAILER_WINDOW_BYTES = 1024;
    private static final int MAX_FILE_NAME_LENGTH = 255;

    private final FileStorage storage;

    public List<StoredPdf> storeAll(List<MultipartFile> files, String keyPrefix) {
        if (files == null || files.isEmpty()) {
            throw ApiException.badRequest("At least one PDF file is required");
        }
        files.forEach(this::requireValidPdf);
        List<String> storedKeys = new ArrayList<>();
        removeUnlessCommitted(storedKeys);
        return files.stream().map(file -> store(file, keyPrefix, storedKeys)).toList();
    }

    public void deleteAfterCommit(String objectKey) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(objectKey);
            }
        });
    }

    private StoredPdf store(MultipartFile file, String keyPrefix, List<String> storedKeys) {
        String objectKey = keyPrefix + UUID.randomUUID() + ".pdf";
        try (InputStream in = file.getInputStream()) {
            storage.put(objectKey, in, file.getSize(), CONTENT_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storedKeys.add(objectKey);
        return new StoredPdf(fileNameOf(file), objectKey, file.getSize());
    }

    private void removeUnlessCommitted(List<String> storedKeys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storedKeys.forEach(PdfStorage.this::deleteQuietly);
                }
            }
        });
    }

    private void deleteQuietly(String objectKey) {
        try {
            storage.delete(objectKey);
        } catch (StorageException e) {
            log.warn("Could not delete object {}; it needs manual cleanup", objectKey, e);
        }
    }

    private void requireValidPdf(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (file.isEmpty() || name == null || !name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw ApiException.badRequest("Only non-empty PDF files are allowed: " + name);
        }
        if (fileNameOf(file).length() > MAX_FILE_NAME_LENGTH) {
            throw ApiException.badRequest("File name is too long: " + name);
        }
        if (!hasPdfStructure(file)) {
            throw ApiException.badRequest("File is not a valid PDF: " + name);
        }
    }

    private boolean hasPdfStructure(MultipartFile file) {
        try {
            return startsWithHeader(file) && endsWithTrailer(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean startsWithHeader(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return Arrays.equals(in.readNBytes(HEADER.length), HEADER);
        }
    }

    private boolean endsWithTrailer(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            in.skipNBytes(Math.max(0, file.getSize() - TRAILER_WINDOW_BYTES));
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1).contains(TRAILER);
        }
    }

    private String fileNameOf(MultipartFile file) {
        return StringUtils.getFilename(StringUtils.cleanPath(file.getOriginalFilename()));
    }
}
