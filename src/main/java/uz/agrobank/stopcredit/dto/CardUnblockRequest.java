package uz.agrobank.stopcredit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CardUnblockRequest(
        @NotBlank @Size(max = 500) String orderNumber,
        @Size(max = 500) String comment) {
}