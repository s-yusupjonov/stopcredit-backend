package uz.agrobank.stopcredit.repository;

import org.springframework.data.jpa.domain.Specification;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.dto.CreditFilter;

import java.time.Instant;
import java.util.Set;

public final class CreditSpecifications {

    private CreditSpecifications() {
    }

    public static Specification<Credit> build(CreditFilter filter, Set<CreditStage> visibleStages, Instant now) {
        return Specification.where(inStages(visibleStages))
                .and(equalTo("status", filter.status()))
                .and(equalTo("type", filter.type()))
                .and(equalTo("stage", filter.stage()))
                .and(overdue(filter.danger(), now))
                .and(matchesText(filter.q()));
    }

    public static Specification<Credit> inStages(Set<CreditStage> stages) {
        return (root, query, cb) -> stages.isEmpty() ? cb.disjunction() : root.get("stage").in(stages);
    }

    private static Specification<Credit> equalTo(String attribute, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(attribute), value);
    }

    public static Specification<Credit> overdue(Instant now) {
        return (root, query, cb) -> cb.lessThan(root.<Instant>get("stageDeadline"), now);
    }

    private static Specification<Credit> overdue(Boolean danger, Instant now) {
        return Boolean.TRUE.equals(danger) ? overdue(now) : null;
    }

    private static Specification<Credit> matchesText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.<String>get("firstName")), pattern),
                cb.like(cb.lower(root.<String>get("lastName")), pattern),
                cb.like(cb.lower(root.<String>get("middleName")), pattern),
                cb.like(root.<String>get("pinfl"), pattern),
                cb.like(cb.lower(root.<String>get("applicationNumber")), pattern));
    }
}
