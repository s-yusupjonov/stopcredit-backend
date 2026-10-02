package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.domain.Card;
import uz.agrobank.stopcredit.domain.Executor;
import uz.agrobank.stopcredit.dto.CardFilter;
import uz.agrobank.stopcredit.dto.CardRequest;
import uz.agrobank.stopcredit.dto.CardResponse;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CardMapper;
import uz.agrobank.stopcredit.repository.CardRepository;
import uz.agrobank.stopcredit.repository.CardSpecifications;
import uz.agrobank.stopcredit.repository.ExecutorRepository;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final ExecutorRepository executorRepository;
    private final UserRepository userRepository;
    private final CardMapper mapper;
    private final CardDocumentService documentService;

    @Transactional
    public CardResponse create(AuthUser user, CardRequest request) {
        Card card = new Card();
        mapper.apply(card, request, findExecutor(request.executorId()));
        card.setSenderName(senderName(user));
        return mapper.toResponse(cardRepository.saveAndFlush(card), null);
    }

    @Transactional
    public CardResponse update(Long id, CardRequest request) {
        Card card = find(id);
        mapper.apply(card, request, findExecutor(request.executorId()));
        return mapper.toResponse(cardRepository.saveAndFlush(card), null);
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
        return cardRepository.findAll(CardSpecifications.build(filter), Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(card -> mapper.toResponse(card, null))
                .toList();
    }

    private Card find(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Card not found: " + id));
    }

    private Executor findExecutor(Long id) {
        return executorRepository.findById(id)
                .orElseThrow(() -> ApiException.badRequest("Executor not found: " + id));
    }

    private String senderName(AuthUser user) {
        return userRepository.findById(user.id())
                .orElseThrow(() -> ApiException.unauthorized("User not found"))
                .getFullName();
    }
}
