package uz.agrobank.stopcredit.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.LoginRequest;
import uz.agrobank.stopcredit.exception.ApiException;
import uz.agrobank.stopcredit.mapper.UserMapper;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.JwtService;
import uz.agrobank.stopcredit.security.LoginAttemptLimiter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final LoginAttemptLimiter limiter = mock(LoginAttemptLimiter.class);
    private final AuthService service = new AuthService(userRepository, passwordEncoder, mock(JwtService.class),
            new UserMapper(), mock(LdapAuthenticationService.class), mock(LdapUserProvisioningService.class),
            limiter);

    @Test
    void inactiveAccountWithCorrectPasswordGetsForbiddenNotInvalidCredentials() {
        User user = localUser(false);
        when(userRepository.findByUsernameIgnoreCase("ali")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("ali", "secret")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(403));
        verify(limiter, never()).recordFailure(anyString());
    }

    @Test
    void wrongPasswordCountsAsFailure() {
        User user = localUser(true);
        when(userRepository.findByUsernameIgnoreCase("Ali")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest(" Ali ", "wrong")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(401));
        verify(limiter).recordFailure("ali");
    }

    private User localUser(boolean active) {
        User user = new User();
        user.setUsername("ali");
        user.setFullName("Ali Valiyev");
        user.setPasswordHash("hash");
        user.setRole(Role.ANTI_FRAUD);
        user.setActive(active);
        return user;
    }
}
