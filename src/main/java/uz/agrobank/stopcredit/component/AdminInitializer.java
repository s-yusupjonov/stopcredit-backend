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

@Slf4j
@Component
@RequiredArgsConstructor
class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        User admin = new User();
        admin.setUsername(properties.defaultUsername());
        admin.setPasswordHash(passwordEncoder.encode(properties.defaultPassword()));
        admin.setFullName("System Administrator");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.warn("Default admin '{}' created - change its password", properties.defaultUsername());
    }
}
