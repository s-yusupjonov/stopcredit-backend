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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.agrobank.stopcredit.component.CreditExcelExporter;
import uz.agrobank.stopcredit.dto.CreditFilter;
import uz.agrobank.stopcredit.dto.CreditRequest;
import uz.agrobank.stopcredit.dto.CreditResponse;
import uz.agrobank.stopcredit.dto.CreditSummary;
import uz.agrobank.stopcredit.dto.StatusUpdateRequest;
import uz.agrobank.stopcredit.security.AuthUser;
import uz.agrobank.stopcredit.service.CreditService;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final CreditService creditService;
    private final CreditExcelExporter excelExporter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreditResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreditRequest request) {
        return creditService.create(user, request);
    }

    @GetMapping
    public PagedModel<CreditResponse> list(
            @AuthenticationPrincipal AuthUser user,
            @ModelAttribute CreditFilter filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(creditService.search(user, filter, pageable));
    }

    @GetMapping("/summary")
    public CreditSummary summary(@AuthenticationPrincipal AuthUser user, @ModelAttribute CreditFilter filter) {
        return creditService.summary(user, filter);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal AuthUser user, @ModelAttribute CreditFilter filter) {
        byte[] content = excelExporter.export(creditService.searchAll(user, filter));
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("credits.xlsx").build().toString())
                .body(content);
    }

    @GetMapping("/{id}")
    public CreditResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return creditService.get(user, id);
    }

    @PutMapping("/{id}")
    public CreditResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                 @Valid @RequestBody CreditRequest request) {
        return creditService.update(user, id, request);
    }

    @PatchMapping("/{id}/status")
    public CreditResponse updateStatus(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                       @Valid @RequestBody StatusUpdateRequest request) {
        return creditService.updateStatus(user, id, request.status());
    }

    @PostMapping("/{id}/advance")
    public CreditResponse advance(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return creditService.advance(user, id);
    }
}
