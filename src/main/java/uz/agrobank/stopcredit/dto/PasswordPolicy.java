package uz.agrobank.stopcredit.dto;

public final class PasswordPolicy {

    public static final String OPTIONAL_PASSWORD = "^$|.{6,72}";
    public static final String MESSAGE = "Parol 6 dan 72 tagacha belgidan iborat bo'lishi kerak";

    private PasswordPolicy() {
    }
}
