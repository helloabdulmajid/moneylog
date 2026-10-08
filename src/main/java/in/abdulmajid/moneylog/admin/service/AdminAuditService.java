package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.dto.response.AdminAuditLogResponse;
import in.abdulmajid.moneylog.admin.model.AuditLog;
import in.abdulmajid.moneylog.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<AdminAuditLogResponse> list(String action,
                                            LocalDateTime from,
                                            LocalDateTime to,
                                            int page,
                                            int size) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (action != null && !action.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("action")),
                        "%" + action.trim().toLowerCase() + "%"));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return auditLogRepository.findAll(spec,
                        PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(AdminAuditService::toDto);
    }

    static AdminAuditLogResponse toDto(AuditLog log) {
        return AdminAuditLogResponse.builder()
                .id(log.getId())
                .adminId(log.getAdmin() != null ? log.getAdmin().getId() : null)
                .adminEmail(log.getAdmin() != null ? log.getAdmin().getEmail() : null)
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .ip(log.getIp())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
