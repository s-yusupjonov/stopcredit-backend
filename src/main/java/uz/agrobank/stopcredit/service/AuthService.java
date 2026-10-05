package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.agrobank.stopcredit.domain.AuthSource;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.LoginRequest;
import uz.agrobank.stopcredit.dto.LoginResponse;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.UserMapper;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.JwtService;

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

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        User user = authenticate(username, request.password());

        if (!user.isActive()) {
            throw ApiException.unauthorized("Invalid username or password");
        }

        return new LoginResponse(jwtService.generate(user), userMapper.toResponse(user));
    }

    private User authenticate(String username, String password) {

        Optional<User> existing = userRepository.findByUsername(username);

        if (existing.isPresent() && existing.get().getAuthSource() == AuthSource.LOCAL) {
            return authenticateLocal(existing.get(), password);
        }

        return authenticateAd(username, password);
    }

    private User authenticateLocal(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid username or password");
        }
        return user;
    }

    private User authenticateAd(String username, String password) {
        if (!ldapAuthenticationService.authenticate(username, password)) {
            throw ApiException.unauthorized("Invalid username or password");
        }
        return ldapUserProvisioningService.provision(
                username,
                () -> ldapAuthenticationService.resolveFullName(username).orElse(username));
    }
}