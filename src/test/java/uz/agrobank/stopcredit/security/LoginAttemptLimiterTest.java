package uz.agrobank.stopcredit.security;

import org.junit.jupiter.api.Test;
import uz.agrobank.stopcredit.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptLimiterTest {

    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter();

    @Test
    void blocksAfterRepeatedFailures() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("alice");
        }

        assertThatThrownBy(() -> limiter.checkAllowed("alice"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatus().value()).isEqualTo(429));
    }

    @Test
    void allowsBelowThresholdAndOtherUsers() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("alice");
        }

        assertThatCode(() -> limiter.checkAllowed("alice")).doesNotThrowAnyException();
        assertThatCode(() -> limiter.checkAllowed("bob")).doesNotThrowAnyException();
    }

    @Test
    void resetClearsFailures() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("alice");
        }
        limiter.reset("alice");

        assertThatCode(() -> limiter.checkAllowed("alice")).doesNotThrowAnyException();
    }
}
