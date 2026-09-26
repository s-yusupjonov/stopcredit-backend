package uz.agrobank.stopcredit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByRole(Role role);
}
