package uz.agrobank.stopcredit.component;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.agrobank.stopcredit.config.AdminProperties;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void generatesAPasswordWhenNoneIsConfigured() {
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        new AdminInitializer(userRepository, passwordEncoder, new AdminProperties("admin", "")).run(null);

        ArgumentCaptor<String> password = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(password.capture());
        assertThat(password.getValue()).hasSizeGreaterThanOrEqualTo(20).isNotEqualTo("changeme");
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void doesNotCrashWhenTheLoginBelongsToAnotherRole() {
        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        new AdminInitializer(userRepository, passwordEncoder, new AdminProperties("admin", "x")).run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void skipsWhenAnAdminAlreadyExists() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        new AdminInitializer(userRepository, passwordEncoder, new AdminProperties("admin", "")).run(null);

        verify(userRepository, never()).save(any());
    }
}
