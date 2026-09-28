package uz.agrobank.stopcredit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.agrobank.stopcredit.domain.CardDocument;

import java.util.List;
import java.util.Optional;

public interface CardDocumentRepository extends JpaRepository<CardDocument, Long> {

    List<CardDocument> findByCardIdOrderByUploadedAtAsc(Long cardId);

    Optional<CardDocument> findByIdAndCardId(Long id, Long cardId);
}
