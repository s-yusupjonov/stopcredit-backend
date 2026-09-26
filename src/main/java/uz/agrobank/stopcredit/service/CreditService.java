package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.config.CreditProperties;
import uz.agrobank.stopcredit.domain.Credit;
import uz.agrobank.stopcredit.domain.CreditStage;
import uz.agrobank.stopcredit.domain.CreditStatus;
import uz.agrobank.stopcredit.dto.CreditFilter;
import uz.agrobank.stopcredit.dto.CreditRequest;
import uz.agrobank.stopcredit.dto.CreditResponse;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CreditMapper;
import uz.agrobank.stopcredit.repository.CreditRepository;
import uz.agrobank.stopcredit.repository.CreditSpecifications;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final CreditAccess access;
    private final CreditMapper mapper;
    private final CreditDocumentService documentService;
    private final CreditProperties properties;

    @Transactional
    public CreditResponse create(AuthUser user, CreditRequest request) {
        if (creditRepository.existsByApplicationNumber(request.applicationNumber().trim())) {
            throw duplicateApplicationNumber(request.applicationNumber());
        }
        Credit credit = new Credit();
        mapper.apply(credit, request);
        credit.setStage(CreditStage.ANTI_FRAUD);
        credit.setCreatedBy(userRepository.getReferenceById(user.id()));
        return toResponse(creditRepository.saveAndFlush(credit));
    }

    @Transactional
    public CreditResponse update(AuthUser user, Long id, CreditRequest request) {
        Credit credit = access.loadForWork(user, id);
        if (creditRepository.existsByApplicationNumberAndIdNot(request.applicationNumber().trim(), id)) {
            throw duplicateApplicationNumber(request.applicationNumber());
        }
        mapper.apply(credit, request);
        return toResponse(creditRepository.saveAndFlush(credit));
    }

    @Transactional
    public CreditResponse updateStatus(AuthUser user, Long id, CreditStatus status) {
        Credit credit = access.loadVisible(user, id);
        credit.setStatus(status);
        return toResponse(creditRepository.saveAndFlush(credit));
    }

    @Transactional
    public CreditResponse advance(AuthUser user, Long id) {
        Credit credit = access.loadForWork(user, id);
        if (!documentService.existsForStage(credit.getId(), credit.getStage())) {
            throw ApiException.badRequest("Upload at least one PDF document before forwarding the credit");
        }
        CreditStage next = credit.getStage().next();
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
        return creditRepository.findAll(specification(user, filter, now), Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(credit -> mapper.toResponse(credit, now, null))
                .toList();
    }

    private Specification<Credit> specification(AuthUser user, CreditFilter filter, Instant now) {
        return CreditSpecifications.build(filter, CreditStage.visibleTo(user.role()), now);
    }

    private CreditResponse toResponse(Credit credit) {
        return mapper.toResponse(credit, Instant.now(), null);
    }

    private ApiException duplicateApplicationNumber(String applicationNumber) {
        return ApiException.conflict("Application number already exists: " + applicationNumber);
    }
}
