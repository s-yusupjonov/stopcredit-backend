package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.UserCreateRequest;
import uz.agrobank.stopcredit.dto.UserResponse;
import uz.agrobank.stopcredit.dto.UserUpdateRequest;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.UserMapper;
import uz.agrobank.stopcredit.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Username already exists: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        return mapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll(Sort.by("id")).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found: " + id));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setActive(request.active());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return mapper.toResponse(userRepository.saveAndFlush(user));
    }
}
