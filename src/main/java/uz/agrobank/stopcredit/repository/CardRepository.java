package uz.agrobank.stopcredit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.agrobank.stopcredit.domain.Card;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    @Override
    @EntityGraph(attributePaths = "executor")
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "executor")
    List<Card> findAll(Specification<Card> spec, Sort sort);
}
