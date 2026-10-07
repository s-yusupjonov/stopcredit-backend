package uz.agrobank.stopcredit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uz.agrobank.stopcredit.exception.ApiException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PdfStorageTest {

    private static final String VALID_PDF = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n";

    private final FileStorage storage = mock(FileStorage.class);
    private final PdfStorage pdfStorage = new PdfStorage(storage);

    @BeforeEach
    void startTransaction() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void endTransaction() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void storesValidPdf() {
        List<StoredPdf> stored = pdfStorage.storeAll(List.of(pdf("order.pdf", VALID_PDF)), "credits/1/");

        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).fileName()).isEqualTo("order.pdf");
        assertThat(stored.get(0).objectKey()).startsWith("credits/1/").endsWith(".pdf");
        verify(storage).put(eq(stored.get(0).objectKey()), any(), anyLong(), eq("application/pdf"));
    }

    @Test
    void rejectsFileWithOnlyPdfPrefix() {
        assertThatThrownBy(() -> pdfStorage.storeAll(List.of(pdf("fake.pdf", "%PDF-fake")), "p/"))
                .isInstanceOf(ApiException.class);
        verify(storage, never()).put(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void rejectsNonPdfExtensionAndEmptyRequest() {
        assertThatThrownBy(() -> pdfStorage.storeAll(List.of(pdf("order.txt", VALID_PDF)), "p/"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> pdfStorage.storeAll(List.of(), "p/")).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsTooLongFileName() {
        String longName = "a".repeat(260) + ".pdf";

        assertThatThrownBy(() -> pdfStorage.storeAll(List.of(pdf(longName, VALID_PDF)), "p/"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void removesStoredObjectsWhenTransactionRollsBack() {
        StoredPdf stored = pdfStorage.storeAll(List.of(pdf("order.pdf", VALID_PDF)), "p/").get(0);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(storage).delete(stored.objectKey());
    }

    @Test
    void keepsStoredObjectsWhenTransactionCommits() {
        pdfStorage.storeAll(List.of(pdf("order.pdf", VALID_PDF)), "p/");

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));

        verify(storage, never()).delete(anyString());
    }

    private MockMultipartFile pdf(String name, String content) {
        return new MockMultipartFile("files", name, "application/pdf", content.getBytes(StandardCharsets.ISO_8859_1));
    }
}
