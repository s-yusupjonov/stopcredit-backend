package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExecutorRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 50) String phone,
        @Size(max = 50) String extension) {
}
