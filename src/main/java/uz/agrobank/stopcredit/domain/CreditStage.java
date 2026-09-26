package uz.agrobank.stopcredit.domain;

import java.util.EnumSet;
import java.util.Set;

public enum CreditStage {
    ANTI_FRAUD(Role.ANTI_FRAUD),
    CREDIT_MANAGEMENT(Role.CREDIT_MANAGEMENT),
    LEGAL(Role.LEGAL),
    UNDERWRITING(Role.UNDERWRITING),
    COMPLETED(null);

    private final Role owner;

    CreditStage(Role owner) {
        this.owner = owner;
    }

    public boolean isOwnedBy(Role role) {
        return owner != null && owner == role;
    }

    public boolean isReviewStage() {
        return this == CREDIT_MANAGEMENT || this == LEGAL || this == UNDERWRITING;
    }

    public CreditStage next() {
        if (this == COMPLETED) {
            throw new IllegalStateException("Credit is already completed");
        }
        return values()[ordinal() + 1];
    }

    // Departments see the credits directed to them and the ones they have already processed
    public static Set<CreditStage> visibleTo(Role role) {
        return switch (role) {
            case ANTI_FRAUD, MANAGEMENT -> EnumSet.allOf(CreditStage.class);
            case CREDIT_MANAGEMENT -> EnumSet.range(CREDIT_MANAGEMENT, COMPLETED);
            case LEGAL -> EnumSet.range(LEGAL, COMPLETED);
            case UNDERWRITING -> EnumSet.range(UNDERWRITING, COMPLETED);
            case ADMIN -> EnumSet.noneOf(CreditStage.class);
        };
    }
}
