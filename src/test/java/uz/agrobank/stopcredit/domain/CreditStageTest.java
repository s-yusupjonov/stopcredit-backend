package uz.agrobank.stopcredit.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uz.agrobank.stopcredit.domain.CreditStage.ANTI_FRAUD;
import static uz.agrobank.stopcredit.domain.CreditStage.COMPLETED;
import static uz.agrobank.stopcredit.domain.CreditStage.CREDIT_MANAGEMENT;
import static uz.agrobank.stopcredit.domain.CreditStage.LEGAL;
import static uz.agrobank.stopcredit.domain.CreditStage.UNDERWRITING;

class CreditStageTest {

    @Test
    void workflowAdvancesInOrder() {
        assertEquals(CREDIT_MANAGEMENT, ANTI_FRAUD.next());
        assertEquals(LEGAL, CREDIT_MANAGEMENT.next());
        assertEquals(UNDERWRITING, LEGAL.next());
        assertEquals(COMPLETED, UNDERWRITING.next());
        assertThrows(IllegalStateException.class, COMPLETED::next);
    }

    @Test
    void onlyReviewStagesHaveDeadlines() {
        assertFalse(ANTI_FRAUD.isReviewStage());
        assertTrue(CREDIT_MANAGEMENT.isReviewStage());
        assertTrue(LEGAL.isReviewStage());
        assertTrue(UNDERWRITING.isReviewStage());
        assertFalse(COMPLETED.isReviewStage());
    }

    @Test
    void antiFraudAndManagementSeeEverything() {
        assertEquals(EnumSet.allOf(CreditStage.class), CreditStage.visibleTo(Role.ANTI_FRAUD));
        assertEquals(EnumSet.allOf(CreditStage.class), CreditStage.visibleTo(Role.MANAGEMENT));
    }

    @Test
    void departmentsSeeTheirStageAndLater() {
        assertEquals(EnumSet.of(LEGAL, UNDERWRITING, COMPLETED), CreditStage.visibleTo(Role.LEGAL));
        assertEquals(EnumSet.of(UNDERWRITING, COMPLETED), CreditStage.visibleTo(Role.UNDERWRITING));
    }

    @Test
    void adminSeesNoCredits() {
        assertTrue(CreditStage.visibleTo(Role.ADMIN).isEmpty());
    }

    @Test
    void stageOwnership() {
        assertTrue(LEGAL.isOwnedBy(Role.LEGAL));
        assertFalse(LEGAL.isOwnedBy(Role.MANAGEMENT));
        assertFalse(COMPLETED.isOwnedBy(Role.LEGAL));
    }
}
