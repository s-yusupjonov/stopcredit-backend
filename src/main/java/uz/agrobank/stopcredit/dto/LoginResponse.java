package uz.agrobank.stopcredit.dto;

public record LoginResponse(String token, UserResponse user) {
}
