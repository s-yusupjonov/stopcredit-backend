package uz.agrobank.stopcredit.dto;

public final class PasswordPolicy {

    public static final String OPTIONAL_PASSWORD = "^$|.{6,72}";
    public static final String MESSAGE = "Password must be between 6 and 72 characters";

    private PasswordPolicy() {
    }
}
