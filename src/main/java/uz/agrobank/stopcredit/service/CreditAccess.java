package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.repository.CreditRepository;
import uz.agrobank.stopcredit.security.AuthUser;

@Component
@RequiredArgsConstructor
public class CreditAccess {

    private final CreditRepository creditRepository;

    public Credit loadVisible(AuthUser user, Long id) {
        return creditRepository.findById(id)
                .filter(credit -> CreditStage.visibleTo(user.role()).contains(credit.getStage()))
                .orElseThrow(() -> ApiException.notFound("Credit not found: " + id));
    }

    public Credit loadForWork(AuthUser user, Long id) {
        Credit credit = loadVisible(user, id);
        if (!credit.getStage().isOwnedBy(user.role())) {
            throw ApiException.forbidden("Credit is not at your department's stage");
        }
        return credit;
    }
}
