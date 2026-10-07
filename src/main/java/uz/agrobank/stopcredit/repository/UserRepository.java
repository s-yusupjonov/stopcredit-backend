package uz.agrobank.stopcredit.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import uz.agrobank.stopcredit.domain.Role;
import uz.agrobank.stopcredit.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByRole(Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.role = uz.agrobank.stopcredit.domain.Role.ADMIN and u.active = true order by u.id")
    List<User> lockActiveAdmins();
}
