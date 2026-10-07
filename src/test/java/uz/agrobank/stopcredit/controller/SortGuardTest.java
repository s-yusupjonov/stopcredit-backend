package uz.agrobank.stopcredit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uz.agrobank.stopcredit.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortGuardTest {

    @Test
    void allowsWhitelistedProperties() {
        assertThatCode(() -> SortGuard.allowOnly(PageRequest.of(0, 10, Sort.by("createdAt", "amount")),
                SortGuard.CREDIT_PROPERTIES)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNestedOrUnknownProperties() {
        assertThatThrownBy(() -> SortGuard.allowOnly(PageRequest.of(0, 10, Sort.by("createdBy.passwordHash")),
                SortGuard.CREDIT_PROPERTIES)).isInstanceOf(ApiException.class);
    }
}
