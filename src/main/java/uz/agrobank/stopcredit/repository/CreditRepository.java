package uz.agrobank.stopcredit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.agrobank.stopcredit.domain.Credit;

import java.util.List;

public interface CreditRepository extends JpaRepository<Credit, Long>, JpaSpecificationExecutor<Credit> {

    boolean existsByApplicationNumber(String applicationNumber);

    boolean existsByApplicationNumberAndIdNot(String applicationNumber, Long id);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    Page<Credit> findAll(Specification<Credit> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    List<Credit> findAll(Specification<Credit> spec, Sort sort);
}
