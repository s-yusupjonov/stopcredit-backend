package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.config.CreditProperties;
import uz.agrobank.stopcredit.domain.AuditAction;
import uz.agrobank.stopcredit.domain.AuditEntity;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.dto.CreditFilter;
import uz.agrobank.stopcredit.dto.CreditRequest;
import uz.agrobank.stopcredit.dto.CreditResponse;
import uz.agrobank.stopcredit.dto.CreditSummary;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CreditMapper;
import uz.agrobank.stopcredit.repository.CreditRepository;
import uz.agrobank.stopcredit.repository.CreditSpecifications;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static uz.agrobank.stopcredit.service.AuditService.change;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final CreditAccess access;
    private final CreditMapper mapper;
    private final CreditDocumentService documentService;
    private final CreditProperties properties;
    private final AuditService audit;

    @Transactional
    public CreditResponse create(AuthUser user, CreditRequest request) {
        if (creditRepository.existsByApplicationNumber(request.applicationNumber().trim())) {
            throw duplicateApplicationNumber(request.applicationNumber());
        }
        Credit credit = new Credit();
        mapper.apply(credit, request);
        credit.setStatus(request.status());
        credit.setStage(CreditStage.ANTI_FRAUD);
        credit.setCreatedBy(userRepository.getReferenceById(user.id()));
        Credit saved = creditRepository.saveAndFlush(credit);
        audit.record(user, AuditEntity.CREDIT, saved.getId(), AuditAction.CREATE);
        return toResponse(saved);
    }

    @Transactional
    public CreditResponse update(AuthUser user, Long id, CreditRequest request) {
        Credit credit = access.loadForWork(user, id);
        StaleCheck.requireCurrent(request.version(), credit.getVersion());
        if (request.status() != credit.getStatus()) {
            throw ApiException.badRequest("Kredit statusini faqat Kredit boshqaruvi bo'limi o'zgartira oladi");
        }
        if (creditRepository.existsByApplicationNumberAndIdNot(request.applicationNumber().trim(), id)) {
            throw duplicateApplicationNumber(request.applicationNumber());
        }
        mapper.apply(credit, request);
        audit.record(user, AuditEntity.CREDIT, id, AuditAction.UPDATE);
        return toResponse(creditRepository.saveAndFlush(credit));
    }

    @Transactional
    public CreditResponse updateStatus(AuthUser user, Long id, CreditStatus status) {
        Credit credit = access.loadVisibleForUpdate(user, id);
        if (credit.getStatus() == status) {
            return toResponse(credit);
        }
        audit.record(user, AuditEntity.CREDIT, id, AuditAction.STATUS_CHANGE, change(credit.getStatus(), status));
        credit.setStatus(status);
        return toResponse(creditRepository.saveAndFlush(credit));
    }

    @Transactional
    public CreditResponse advance(AuthUser user, Long id) {
        Credit credit = access.loadForWork(user, id);
        if (!documentService.existsForStage(credit.getId(), credit.getStage())) {
            throw ApiException.badRequest("Keyingi bosqichga yuborishdan oldin kamida bitta PDF hujjat yuklang");
        }
        CreditStage next = credit.getStage().next();
        audit.record(user, AuditEntity.CREDIT, id, AuditAction.ADVANCE, change(credit.getStage(), next));
        credit.setStage(next);
        credit.setStageDeadline(next.isReviewStage()
                ? Instant.now().plus(Duration.ofDays(properties.reviewDeadlineDays()))
                : null);
        return toResponse(creditRepository.saveAndFlush(credit));
    }

    @Transactional(readOnly = true)
    public CreditResponse get(AuthUser user, Long id) {
        Credit credit = access.loadVisible(user, id);
        return mapper.toResponse(credit, Instant.now(), documentService.findByCredit(credit.getId()));
    }

    @Transactional(readOnly = true)
    public Page<CreditResponse> search(AuthUser user, CreditFilter filter, Pageable pageable) {
        Instant now = Instant.now();
        return creditRepository.findAll(specification(user, filter, now), pageable)
                .map(credit -> mapper.toResponse(credit, now, null));
    }

    @Transactional(readOnly = true)
    public List<CreditResponse> searchAll(AuthUser user, CreditFilter filter) {
        Instant now = Instant.now();
        Specification<Credit> specification = specification(user, filter, now);
        ExportLimit.require(creditRepository.count(specification));
        return creditRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(credit -> mapper.toResponse(credit, now, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditSummary summary(AuthUser user, CreditFilter filter) {
        Instant now = Instant.now();
        Specification<Credit> base = specification(user, filter, now);
        return new CreditSummary(
                creditRepository.count(base),
                creditRepository.count(base.and(CreditSpecifications.overdue(now))),
                creditRepository.count(base.and(CreditSpecifications.inStages(CreditStage.ownedBy(user.role())))),
                creditRepository.count(base.and(CreditSpecifications.inStages(Set.of(CreditStage.COMPLETED)))));
    }

    private Specification<Credit> specification(AuthUser user, CreditFilter filter, Instant now) {
        return CreditSpecifications.build(filter, CreditStage.visibleTo(user.role()), now);
    }

    private CreditResponse toResponse(Credit credit) {
        return mapper.toResponse(credit, Instant.now(), null);
    }

    private ApiException duplicateApplicationNumber(String applicationNumber) {
        return ApiException.conflict("Bunday ariza raqami allaqachon mavjud: " + applicationNumber.trim());
    }
}
