package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uz.agrobank.stopcredit.domain.Role;

public record UserUpdateRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotNull Role role,
        @NotNull Boolean active,
        @Pattern(regexp = PasswordPolicy.OPTIONAL_PASSWORD, message = PasswordPolicy.MESSAGE) String password) {
}
