package uz.agrobank.stopcredit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.agrobank.stopcredit.dto.ExecutorRequest;
import uz.agrobank.stopcredit.dto.ExecutorResponse;
import uz.agrobank.stopcredit.service.ExecutorDirectoryService;

import java.util.List;

@RestController
@RequestMapping("/api/executors")
@RequiredArgsConstructor
public class ExecutorController {

    private final ExecutorDirectoryService executorService;

    @GetMapping
    public List<ExecutorResponse> list() {
        return executorService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutorResponse create(@Valid @RequestBody ExecutorRequest request) {
        return executorService.create(request);
    }
}
