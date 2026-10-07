package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.domain.AuditAction;
import uz.agrobank.stopcredit.domain.AuditEntity;
import uz.agrobank.stopcredit.domain.AuthSource;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.AdLookupResponse;
import uz.agrobank.stopcredit.dto.UserCreateRequest;
import uz.agrobank.stopcredit.dto.UserResponse;
import uz.agrobank.stopcredit.dto.UserUpdateRequest;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.UserMapper;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;

import java.util.ArrayList;
import java.util.List;

import static uz.agrobank.stopcredit.service.AuditService.change;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final LdapAuthenticationService ldapAuthenticationService;
    private final AuditService audit;

    @Transactional
    public UserResponse create(AuthUser actor, UserCreateRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw ApiException.conflict("Bunday login allaqachon mavjud: " + username);
        }
        User user = request.authSource() == AuthSource.AD
                ? newAdUser(username)
                : newLocalUser(username, request);
        user.setRole(request.role());
        User saved = userRepository.saveAndFlush(user);
        audit.record(actor, AuditEntity.USER, saved.getId(), AuditAction.CREATE);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll(Sort.by("id")).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdLookupResponse lookupAdAccount(String username) {
        String name = username.trim();
        if (name.isEmpty()) {
            throw ApiException.badRequest("Login kiritilishi shart");
        }
        String fullName = ldapAuthenticationService.lookupFullName(name)
                .orElseThrow(() -> ApiException.notFound("Active Directory'da bunday login topilmadi: " + name));
        return new AdLookupResponse(name, fullName, userRepository.existsByUsernameIgnoreCase(name));
    }

    @Transactional(readOnly = true)
    public String fullNameOf(AuthUser actor) {
        return userRepository.findById(actor.id())
                .orElseThrow(() -> ApiException.unauthorized("Foydalanuvchi topilmadi"))
                .getFullName();
    }

    @Transactional
    public UserResponse update(AuthUser actor, Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Foydalanuvchi topilmadi: " + id));
        boolean passwordChange = request.password() != null && !request.password().isBlank();
        if (passwordChange && user.getAuthSource() == AuthSource.AD) {
            throw ApiException.badRequest("AD foydalanuvchisi o'z AD paroli bilan kiradi, unga lokal parol o'rnatib bo'lmaydi");
        }
        requireAnotherActiveAdmin(user, request);

        String changes = describeChanges(user, request);
        boolean deactivated = user.isActive() && !request.active();

        user.setFullName(request.fullName().trim());
        user.setRole(request.role());
        user.setActive(request.active());
        if (passwordChange) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (passwordChange || deactivated) {
            user.revokeTokens();
        }

        audit.record(actor, AuditEntity.USER, id, AuditAction.UPDATE, changes);
        if (passwordChange) {
            audit.record(actor, AuditEntity.USER, id, AuditAction.PASSWORD_RESET);
        }
        return mapper.toResponse(userRepository.saveAndFlush(user));
    }

    private User newLocalUser(String username, UserCreateRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw ApiException.badRequest("Lokal foydalanuvchi uchun parol majburiy");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        return user;
    }

    private User newAdUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(ldapAuthenticationService.lookupFullName(username)
                .orElseThrow(() -> ApiException.badRequest("Active Directory'da bunday login topilmadi: " + username)));
        user.setAuthSource(AuthSource.AD);
        return user;
    }

    private void requireAnotherActiveAdmin(User user, UserUpdateRequest request) {
        boolean losesAdminAccess = user.getRole() == Role.ADMIN && user.isActive()
                && (request.role() != Role.ADMIN || !request.active());
        if (losesAdminAccess && userRepository.lockActiveAdmins().size() <= 1) {
            throw ApiException.conflict("Tizimda kamida bitta faol administrator qolishi kerak");
        }
    }

    private String describeChanges(User user, UserUpdateRequest request) {
        List<String> changes = new ArrayList<>();
        if (user.getRole() != request.role()) {
            changes.add("role " + change(user.getRole(), request.role()));
        }
        if (user.isActive() != request.active()) {
            changes.add("active " + change(user.isActive(), request.active()));
        }
        return changes.isEmpty() ? null : String.join(", ", changes);
    }
}
