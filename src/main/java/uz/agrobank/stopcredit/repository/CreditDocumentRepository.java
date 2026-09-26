package uz.agrobank.stopcredit.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.agrobank.stopcredit.domain.CreditDocument;
import uz.agrobank.stopcredit.domain.CreditStage;

import java.util.List;
import java.util.Optional;

public interface CreditDocumentRepository extends JpaRepository<CreditDocument, Long> {

    @EntityGraph(attributePaths = "uploadedBy")
    List<CreditDocument> findByCreditIdOrderByUploadedAtAsc(Long creditId);

    boolean existsByCreditIdAndStage(Long creditId, CreditStage stage);

    Optional<CreditDocument> findByIdAndCreditId(Long id, Long creditId);
}
