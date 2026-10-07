package uz.agrobank.stopcredit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.agrobank.stopcredit.config.CreditProperties;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.domain.CreditType;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.CreditRequest;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CreditMapper;
import uz.agrobank.stopcredit.repository.CreditRepository;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreditServiceTest {

    private static final AuthUser ANTI_FRAUD = new AuthUser(1L, "af", Role.ANTI_FRAUD);
    private static final AuthUser CREDIT_MANAGER = new AuthUser(2L, "cm", Role.CREDIT_MANAGEMENT);

    private final CreditRepository creditRepository = mock(CreditRepository.class);
    private final CreditAccess access = mock(CreditAccess.class);
    private final AuditService audit = mock(AuditService.class);
    private final CreditService service = new CreditService(creditRepository, mock(UserRepository.class), access,
            new CreditMapper(), mock(CreditDocumentService.class), new CreditProperties(3), audit);

    private Credit credit;

    @BeforeEach
    void setUp() {
        User creator = new User();
        creator.setFullName("Anti Fraud");
        credit = new Credit();
        credit.setFirstName("Ali");
        credit.setLastName("Valiyev");
        credit.setPinfl("12345678901234");
        credit.setType(CreditType.ONLINE);
        credit.setMfo("00001");
        credit.setApplicationNumber("A-1");
        credit.setAmount(BigDecimal.TEN);
        credit.setStatus(CreditStatus.ACTIVE);
        credit.setStage(CreditStage.ANTI_FRAUD);
        credit.setCreatedBy(creator);
        when(creditRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updateFromStaleScreenIsRejected() {
        when(access.loadForWork(ANTI_FRAUD, 7L)).thenReturn(credit);

        assertThatThrownBy(() -> service.update(ANTI_FRAUD, 7L, request(CreditStatus.ACTIVE, 5L)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));
        verify(creditRepository, never()).saveAndFlush(any());
    }

    @Test
    void generalUpdateCannotChangeStatus() {
        when(access.loadForWork(ANTI_FRAUD, 7L)).thenReturn(credit);

        assertThatThrownBy(() -> service.update(ANTI_FRAUD, 7L, request(CreditStatus.STOPPED, 0L)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        assertThat(credit.getStatus()).isEqualTo(CreditStatus.ACTIVE);
    }

    @Test
    void updateWithCurrentVersionIsApplied() {
        when(access.loadForWork(ANTI_FRAUD, 7L)).thenReturn(credit);

        service.update(ANTI_FRAUD, 7L, request(CreditStatus.ACTIVE, 0L));

        assertThat(credit.getApplicationNumber()).isEqualTo("A-2");
    }

    @Test
    void settingTheSameStatusIsNotAudited() {
        when(access.loadVisibleForUpdate(CREDIT_MANAGER, 7L)).thenReturn(credit);

        service.updateStatus(CREDIT_MANAGER, 7L, CreditStatus.ACTIVE);

        verifyNoInteractions(audit);
        verify(creditRepository, never()).saveAndFlush(any());
    }

    private CreditRequest request(CreditStatus status, Long version) {
        return new CreditRequest("Ali", "Valiyev", null, "12345678901234", CreditType.ONLINE, "00001", "A-2",
                BigDecimal.TEN, status, version);
    }
}
