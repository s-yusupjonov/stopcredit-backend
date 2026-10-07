package uz.agrobank.stopcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuthSource authSource = AuthSource.LOCAL;

    private Instant tokensValidAfter;

    public void revokeTokens() {
        tokensValidAfter = Instant.now();
    }

    public boolean acceptsTokenIssuedAt(Instant issuedAt) {
        return tokensValidAfter == null || !issuedAt.isBefore(tokensValidAfter.truncatedTo(ChronoUnit.SECONDS));
    }
}