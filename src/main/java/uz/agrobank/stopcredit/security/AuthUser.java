package uz.agrobank.stopcredit.security;

import uz.agrobank.stopcredit.domain.Role;

public record AuthUser(Long id, String username, Role role) {
}
