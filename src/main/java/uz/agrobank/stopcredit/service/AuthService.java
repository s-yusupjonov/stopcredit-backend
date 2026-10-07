package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.domain.AuthSource;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.LoginRequest;
import uz.agrobank.stopcredit.dto.LoginResponse;
import uz.agrobank.stopcredit.dto.UserResponse;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.UserMapper;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.AuthUser;
import uz.agrobank.stopcredit.security.JwtService;
import uz.agrobank.stopcredit.security.LoginAttemptLimiter;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final LdapAuthenticationService ldapAuthenticationService;
    private final LdapUserProvisioningService ldapUserProvisioningService;
    private final LoginAttemptLimiter loginAttemptLimiter;

    // Deliberately not transactional: a rejected login must not roll back a freshly provisioned AD user
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        String attemptKey = username.toLowerCase(Locale.ROOT);
        loginAttemptLimiter.checkAllowed(attemptKey);

        User user = authenticateCountingFailures(username, request.password(), attemptKey);
        loginAttemptLimiter.reset(attemptKey);
        // the password was correct, so telling the person why they cannot get in leaks nothing
        if (!user.isActive()) {
            throw ApiException.forbidden("Hisobingiz faol emas. Administrator faollashtirishi kerak");
        }
        return new LoginResponse(jwtService.generate(user), userMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(AuthUser principal) {
        return userRepository.findById(principal.id())
                .map(userMapper::toResponse)
                .orElseThrow(() -> ApiException.unauthorized("Foydalanuvchi topilmadi"));
    }

    private User authenticateCountingFailures(String username, String password, String attemptKey) {
        try {
            return authenticate(username, password);
        } catch (ApiException e) {
            loginAttemptLimiter.recordFailure(attemptKey);
            throw e;
        }
    }

    private User authenticate(String username, String password) {
        Optional<User> existing = userRepository.findByUsernameIgnoreCase(username);

        if (existing.isPresent() && existing.get().getAuthSource() == AuthSource.LOCAL) {
            return authenticateLocal(existing.get(), password);
        }

        return authenticateAd(username, password);
    }

    private User authenticateLocal(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return user;
    }

    private User authenticateAd(String username, String password) {
        if (!ldapAuthenticationService.authenticate(username, password)) {
            throw invalidCredentials();
        }
        return ldapUserProvisioningService.provision(
                username,
                () -> ldapAuthenticationService.lookupFullName(username).orElse(username));
    }

    private ApiException invalidCredentials() {
        return ApiException.unauthorized("Login yoki parol noto'g'ri");
    }
}
