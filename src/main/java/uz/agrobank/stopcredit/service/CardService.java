package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.agrobank.stopcredit.domain.AuditAction;
import uz.agrobank.stopcredit.domain.AuditEntity;
import uz.agrobank.stopcredit.domain.Card;
import uz.agrobank.stopcredit.domain.CardDocumentKind;
import uz.agrobank.stopcredit.domain.CardStatus;
import uz.agrobank.stopcredit.domain.Executor;
import uz.agrobank.stopcredit.dto.CardFilter;
import uz.agrobank.stopcredit.dto.CardRequest;
import uz.agrobank.stopcredit.dto.CardResponse;
import uz.agrobank.stopcredit.dto.CardUnblockRequest;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CardMapper;
import uz.agrobank.stopcredit.repository.CardRepository;
import uz.agrobank.stopcredit.repository.CardSpecifications;
import uz.agrobank.stopcredit.repository.ExecutorRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final ExecutorRepository executorRepository;
    private final CardMapper mapper;
    private final CardDocumentService documentService;
    private final UserService userService;
    private final AuditService audit;

    @Transactional
    public CardResponse create(AuthUser user, CardRequest request) {
        Card card = new Card();
        mapper.apply(card, request, findExecutor(request.executorId()));
        card.setSenderName(userService.fullNameOf(user));
        Card saved = cardRepository.saveAndFlush(card);
        audit.record(user, AuditEntity.CARD, saved.getId(), AuditAction.CREATE);
        return mapper.toResponse(saved, null);
    }

    @Transactional
    public CardResponse update(AuthUser user, Long id, CardRequest request) {
        Card card = findForUpdate(id);
        StaleCheck.requireCurrent(request.version(), card.getVersion());
        if (card.getStatus() != request.status()) {
            throw ApiException.badRequest("Karta statusini faqat \"Blokdan ochish\" amali orqali o'zgartirish mumkin");
        }
        mapper.apply(card, request, findExecutor(request.executorId()));
        audit.record(user, AuditEntity.CARD, id, AuditAction.UPDATE);
        return mapper.toResponse(cardRepository.saveAndFlush(card), null);
    }

    @Transactional
    public CardResponse unblock(AuthUser user, Long id, CardUnblockRequest request, List<MultipartFile> files) {
        Card card = findForUpdate(id);
        if (card.getStatus() != CardStatus.BLOCKED) {
            throw ApiException.badRequest("Faqat bloklangan kartani blokdan ochish mumkin");
        }
        String actor = userService.fullNameOf(user);
        documentService.attach(card, actor, files, CardDocumentKind.UNBLOCK);
        card.setStatus(CardStatus.ACTIVE);
        card.setUnblockOrderNumber(request.orderNumber().trim());
        card.setUnblockComment(mapper.blankToNull(request.comment()));
        card.setUnblockedAt(Instant.now());
        card.setUnblockedBy(actor);
        Card saved = cardRepository.saveAndFlush(card);
        audit.record(user, AuditEntity.CARD, id, AuditAction.UNBLOCK, card.getUnblockOrderNumber());
        return mapper.toResponse(saved, documentService.findByCard(saved.getId()));
    }

    @Transactional(readOnly = true)
    public CardResponse get(Long id) {
        Card card = find(id);
        return mapper.toResponse(card, documentService.findByCard(card.getId()));
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> search(CardFilter filter, Pageable pageable) {
        return cardRepository.findAll(CardSpecifications.build(filter), pageable)
                .map(card -> mapper.toResponse(card, null));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> searchAll(CardFilter filter) {
        Specification<Card> specification = CardSpecifications.build(filter);
        ExportLimit.require(cardRepository.count(specification));
        return cardRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(card -> mapper.toResponse(card, null))
                .toList();
    }

    private Card find(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Karta topilmadi: " + id));
    }

    private Card findForUpdate(Long id) {
        return cardRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.notFound("Karta topilmadi: " + id));
    }

    private Executor findExecutor(Long id) {
        return executorRepository.findById(id)
                .orElseThrow(() -> ApiException.badRequest("Ijrochi topilmadi: " + id));
    }

}