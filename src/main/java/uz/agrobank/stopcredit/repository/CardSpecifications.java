package uz.agrobank.stopcredit.repository;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import uz.agrobank.stopcredit.domain.Card;
import uz.agrobank.stopcredit.dto.CardFilter;

import java.time.LocalDate;

public final class CardSpecifications {

    private CardSpecifications() {
    }

    public static Specification<Card> build(CardFilter filter) {
        return Specification.where(equalTo("status", filter.status()))
                .and(equalTo("restrictionType", filter.restrictionType()))
                .and(equalTo("basisCategory", filter.basisCategory()))
                .and(mfoStartsWith(filter.mfo()))
                .and(executorIs(filter.executorId()))
                .and(dateFrom(filter.dateFrom()))
                .and(dateTo(filter.dateTo()))
                .and(matchesText(filter.q()));
    }

    private static Specification<Card> equalTo(String attribute, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(attribute), value);
    }

    private static Specification<Card> mfoStartsWith(String mfo) {
        if (mfo == null || mfo.isBlank()) {
            return null;
        }
        String pattern = LikePattern.startsWith(mfo.trim());
        return (root, query, cb) -> cb.like(root.<String>get("mfo"), pattern, LikePattern.ESCAPE);
    }

    private static Specification<Card> executorIs(Long executorId) {
        if (executorId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("executor").get("id"), executorId);
    }

    private static Specification<Card> dateFrom(LocalDate from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("restrictionDate"), from);
    }

    private static Specification<Card> dateTo(LocalDate to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("restrictionDate"), to);
    }

    private static Specification<Card> matchesText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = LikePattern.containsIgnoreCase(text.trim());
        String digits = LikePattern.contains(text.replaceAll("\\s", ""));
        char escape = LikePattern.ESCAPE;
        return (root, query, cb) -> {
            var executor = root.join("executor", JoinType.INNER);
            return cb.or(
                    cb.like(root.<String>get("cardNumber"), digits, escape),
                    cb.like(cb.lower(root.<String>get("senderName")), lower, escape),
                    cb.like(cb.lower(root.<String>get("basisComment")), lower, escape),
                    cb.like(cb.lower(root.<String>get("statusComment")), lower, escape),
                    cb.like(cb.lower(root.<String>get("unblockOrderNumber")), lower, escape),
                    cb.like(cb.lower(root.<String>get("unblockComment")), lower, escape),
                    cb.like(cb.lower(executor.<String>get("name")), lower, escape));
        };
    }
}