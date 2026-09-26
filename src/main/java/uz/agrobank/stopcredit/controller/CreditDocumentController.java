package uz.agrobank.stopcredit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.agrobank.stopcredit.dto.DocumentResponse;
import uz.agrobank.stopcredit.dto.DownloadedFile;
import uz.agrobank.stopcredit.security.AuthUser;
import uz.agrobank.stopcredit.service.CreditDocumentService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/credits/{creditId}/documents")
@RequiredArgsConstructor
public class CreditDocumentController {

    private final CreditDocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<DocumentResponse> upload(@AuthenticationPrincipal AuthUser user, @PathVariable Long creditId,
                                         @RequestParam("files") List<MultipartFile> files) {
        return documentService.upload(user, creditId, files);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal AuthUser user,
                                                        @PathVariable Long creditId,
                                                        @PathVariable Long documentId) {
        DownloadedFile file = documentService.download(user, creditId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(new InputStreamResource(file.content()));
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long creditId,
                       @PathVariable Long documentId) {
        documentService.delete(user, creditId, documentId);
    }
}
