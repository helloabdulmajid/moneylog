package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.model.Admin;
import in.abdulmajid.moneylog.admin.model.AuditLog;
import in.abdulmajid.moneylog.admin.repository.AuditLogRepository;
import in.abdulmajid.moneylog.admin.security.AdminContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AdminContextHelper adminContextHelper;

    public void record(String action, String targetType, String targetId, String details) {
        record(adminContextHelper.getCurrentAdmin(), action, targetType, targetId, details);
    }

    public void record(Admin admin, String action, String targetType, String targetId, String details) {
        auditLogRepository.save(AuditLog.builder()
                .admin(admin)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .ip(clientIp())
                .build());
    }

    private String clientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
