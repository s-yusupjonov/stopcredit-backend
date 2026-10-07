package uz.agrobank.stopcredit.security;

import java.time.Instant;

public record TokenClaims(Long userId, Instant issuedAt) {
}
