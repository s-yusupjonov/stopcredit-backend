package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.agrobank.stopcredit.domain.Role;

public record UserCreateRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotBlank @Size(max = 150) String fullName,
        @NotNull Role role) {
}
