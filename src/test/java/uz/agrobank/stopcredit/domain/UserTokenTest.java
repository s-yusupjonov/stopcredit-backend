package uz.agrobank.stopcredit.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UserTokenTest {

    @Test
    void acceptsAnyTokenWhenNeverRevoked() {
        assertThat(new User().acceptsTokenIssuedAt(Instant.EPOCH)).isTrue();
    }

    @Test
    void rejectsTokenIssuedBeforeRevocation() {
        User user = new User();
        user.revokeTokens();

        assertThat(user.acceptsTokenIssuedAt(Instant.now().minus(1, ChronoUnit.HOURS))).isFalse();
    }

    @Test
    void acceptsTokenIssuedAfterRevocation() {
        User user = new User();
        user.revokeTokens();

        assertThat(user.acceptsTokenIssuedAt(Instant.now().plus(1, ChronoUnit.MINUTES))).isTrue();
    }
}
