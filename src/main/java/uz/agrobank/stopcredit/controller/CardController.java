package uz.agrobank.stopcredit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.agrobank.stopcredit.component.CardExcelExporter;
import uz.agrobank.stopcredit.dto.CardFilter;
import uz.agrobank.stopcredit.dto.CardRequest;
import uz.agrobank.stopcredit.dto.CardResponse;
import uz.agrobank.stopcredit.security.AuthUser;
import uz.agrobank.stopcredit.service.CardService;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final CardService cardService;
    private final CardExcelExporter excelExporter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CardRequest request) {
        return cardService.create(user, request);
    }

    @GetMapping
    public PagedModel<CardResponse> list(
            @ModelAttribute CardFilter filter,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(cardService.search(filter, pageable));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@ModelAttribute CardFilter filter) {
        byte[] content = excelExporter.export(cardService.searchAll(filter));
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("cards.xlsx").build().toString())
                .body(content);
    }

    @GetMapping("/{id}")
    public CardResponse get(@PathVariable Long id) {
        return cardService.get(id);
    }

    @PutMapping("/{id}")
    public CardResponse update(@PathVariable Long id, @Valid @RequestBody CardRequest request) {
        return cardService.update(id, request);
    }
}
