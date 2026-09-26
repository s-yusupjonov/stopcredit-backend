package uz.agrobank.stopcredit.dto;

import uz.agrobank.stopcredit.domain.AuthSource;
import uz.agrobank.stopcredit.domain.Role;

import java.time.Instant;

public record UserResponse(
        Long id, String username, String fullName, Role role, boolean active,
        AuthSource authSource, Instant createdAt) {
}