package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.domain.Executor;
import uz.agrobank.stopcredit.dto.ExecutorRequest;
import uz.agrobank.stopcredit.dto.ExecutorResponse;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.CardMapper;
import uz.agrobank.stopcredit.repository.ExecutorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutorDirectoryService {

    private final ExecutorRepository executorRepository;
    private final CardMapper mapper;

    @Transactional(readOnly = true)
    public List<ExecutorResponse> list() {
        return executorRepository.findAll(Sort.by("name", "extension")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ExecutorResponse create(ExecutorRequest request) {
        Executor executor = new Executor();
        mapper.apply(executor, request);
        if (executorRepository.existsByNameAndPhoneAndExtension(
                executor.getName(), executor.getPhone(), executor.getExtension())) {
            throw ApiException.conflict("Bunday ijrochi allaqachon mavjud: " + executor.getName());
        }
        return mapper.toResponse(executorRepository.saveAndFlush(executor));
    }
}
