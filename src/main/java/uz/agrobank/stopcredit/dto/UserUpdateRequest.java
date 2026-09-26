package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.agrobank.stopcredit.domain.Role;

public record UserUpdateRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotNull Role role,
        @NotNull Boolean active,
        @Size(max = 72) String password) {
}
