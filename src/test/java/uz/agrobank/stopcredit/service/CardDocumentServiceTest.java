package uz.agrobank.stopcredit.service;

import org.junit.jupiter.api.Test;
import uz.agrobank.stopcredit.domain.CardDocument;
import uz.agrobank.stopcredit.domain.CardDocumentKind;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CardMapper;
import uz.agrobank.stopcredit.repository.CardDocumentRepository;
import uz.agrobank.stopcredit.repository.CardRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardDocumentServiceTest {

    private final CardDocumentRepository documentRepository = mock(CardDocumentRepository.class);
    private final PdfStorage pdfStorage = mock(PdfStorage.class);
    private final CardDocumentService service = new CardDocumentService(documentRepository,
            mock(CardRepository.class), mock(UserService.class), new CardMapper(), mock(FileStorage.class),
            pdfStorage, mock(AuditService.class));

    @Test
    void unblockOrderCannotBeDeleted() {
        CardDocument order = new CardDocument();
        order.setKind(CardDocumentKind.UNBLOCK);
        order.setObjectKey("cards/1/order.pdf");
        when(documentRepository.findByIdAndCardId(5L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.delete(new AuthUser(1L, "af", Role.ANTI_FRAUD), 1L, 5L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(403));
        verify(documentRepository, never()).delete(any());
        verify(pdfStorage, never()).deleteAfterCommit(any());
    }
}
