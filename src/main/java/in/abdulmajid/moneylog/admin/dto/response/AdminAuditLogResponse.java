package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLogResponse {

    private UUID id;
    private UUID adminId;
    private String adminEmail;
    private String action;
    private String targetType;
    private String targetId;
    private String details;
    private String ip;
    private LocalDateTime createdAt;
}
