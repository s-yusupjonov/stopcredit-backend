package uz.agrobank.stopcredit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.agrobank.stopcredit.domain.Executor;

public interface ExecutorRepository extends JpaRepository<Executor, Long> {

    boolean existsByNameAndPhoneAndExtension(String name, String phone, String extension);
}
