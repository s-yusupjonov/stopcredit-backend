package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.agrobank.stopcredit.domain.AuditAction;
import uz.agrobank.stopcredit.domain.AuditEntity;
import uz.agrobank.stopcredit.domain.AuditEvent;
import uz.agrobank.stopcredit.repository.AuditEventRepository;
import uz.agrobank.stopcredit.security.AuthUser;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository repository;

    public void record(AuthUser actor, AuditEntity entity, Long entityId, AuditAction action) {
        record(actor, entity, entityId, action, null);
    }

    public void record(AuthUser actor, AuditEntity entity, Long entityId, AuditAction action, String details) {
        repository.save(new AuditEvent(entity, entityId, action, details, actor.id()));
    }

    public static String change(Object from, Object to) {
        return from + " -> " + to;
    }
}
