package uz.agrobank.stopcredit.security;

import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.exception.ApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptLimiter {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Attempts current = attempts.get(key);
        if (current != null && !isExpired(current) && current.failures() >= MAX_FAILURES) {
            throw ApiException.tooManyRequests("Too many failed login attempts. Try again later");
        }
    }

    public void recordFailure(String key) {
        if (attempts.size() > CLEANUP_THRESHOLD) {
            attempts.values().removeIf(this::isExpired);
        }
        attempts.compute(key, (k, current) -> current == null || isExpired(current)
                ? new Attempts(1, Instant.now())
                : new Attempts(current.failures() + 1, current.windowStart()));
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private boolean isExpired(Attempts value) {
        return value.windowStart().plus(WINDOW).isBefore(Instant.now());
    }

    private record Attempts(int failures, Instant windowStart) {
    }
}
