package uz.agrobank.stopcredit.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikePatternTest {

    @Test
    void wildcardsTypedByTheUserAreMatchedLiterally() {
        assertThat(LikePattern.contains("50%_a\\b")).isEqualTo("%50\\%\\_a\\\\b%");
    }

    @Test
    void containsIgnoreCaseLowersTheText() {
        assertThat(LikePattern.containsIgnoreCase("ValiYev")).isEqualTo("%valiyev%");
    }

    @Test
    void startsWithOnlyAppendsWildcard() {
        assertThat(LikePattern.startsWith("0012")).isEqualTo("0012%");
    }
}
