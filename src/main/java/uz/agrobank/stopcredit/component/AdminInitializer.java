package uz.agrobank.stopcredit.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.config.AdminProperties;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
class AdminInitializer implements ApplicationRunner {

    private static final int GENERATED_PASSWORD_BYTES = 18;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        String username = properties.defaultUsername() == null ? "" : properties.defaultUsername().trim();
        if (username.isEmpty()) {
            log.error("No ADMIN user exists and ADMIN_USERNAME is empty - the bootstrap admin was not created");
            return;
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            log.error("No ADMIN user exists, but the username '{}' is taken by another role - "
                    + "set ADMIN_USERNAME to a free login to bootstrap an administrator", username);
            return;
        }

        boolean generated = properties.defaultPassword() == null || properties.defaultPassword().isBlank();
        String password = generated ? randomPassword() : properties.defaultPassword();

        User admin = new User();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFullName("System Administrator");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        if (generated) {
            log.warn("Bootstrap admin '{}' created with the one-time password: {} - change it right after login",
                    username, password);
        } else {
            log.warn("Bootstrap admin '{}' created from ADMIN_PASSWORD - change it right after login", username);
        }
    }

    private static String randomPassword() {
        byte[] bytes = new byte[GENERATED_PASSWORD_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
