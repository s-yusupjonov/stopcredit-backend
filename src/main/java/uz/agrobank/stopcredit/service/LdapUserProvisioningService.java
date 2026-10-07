package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uz.agrobank.stopcredit.domain.AuthSource;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.repository.UserRepository;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class LdapUserProvisioningService {

    private final UserRepository userRepository;

    public User provision(String username, Supplier<String> fullNameSupplier) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> createPending(username, fullNameSupplier.get()));
    }

    private User createPending(String username, String fullName) {
        try {
            return userRepository.saveAndFlush(newAdUser(username, fullName));
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> e);
        }
    }

    private User newAdUser(String username, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setAuthSource(AuthSource.AD);
        user.setRole(Role.UNDERWRITING);
        user.setActive(false);
        return user;
    }
}
