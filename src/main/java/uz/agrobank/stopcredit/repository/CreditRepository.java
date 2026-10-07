package uz.agrobank.stopcredit.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.agrobank.stopcredit.domain.Credit;

import java.util.List;
import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long>, JpaSpecificationExecutor<Credit> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Credit c where c.id = :id")
    Optional<Credit> findByIdForUpdate(@Param("id") Long id);

    boolean existsByApplicationNumber(String applicationNumber);

    boolean existsByApplicationNumberAndIdNot(String applicationNumber, Long id);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    Page<Credit> findAll(Specification<Credit> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    List<Credit> findAll(Specification<Credit> spec, Sort sort);
}
