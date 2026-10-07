package uz.agrobank.stopcredit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.agrobank.stopcredit.domain.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
