package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.repository.CreditRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditAccess {

    private final CreditRepository creditRepository;

    public Credit loadVisible(AuthUser user, Long id) {
        return visibleTo(user, creditRepository.findById(id), id);
    }

    public Credit loadVisibleForUpdate(AuthUser user, Long id) {
        return visibleTo(user, creditRepository.findByIdForUpdate(id), id);
    }

    public Credit loadForWork(AuthUser user, Long id) {
        Credit credit = loadVisibleForUpdate(user, id);
        if (!credit.getStage().isOwnedBy(user.role())) {
            throw ApiException.forbidden("Credit is not at your department's stage");
        }
        return credit;
    }

    private Credit visibleTo(AuthUser user, Optional<Credit> credit, Long id) {
        return credit
                .filter(c -> CreditStage.visibleTo(user.role()).contains(c.getStage()))
                .orElseThrow(() -> ApiException.notFound("Credit not found: " + id));
    }
}
