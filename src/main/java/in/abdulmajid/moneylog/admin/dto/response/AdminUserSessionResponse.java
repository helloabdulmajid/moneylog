package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSessionResponse {

    private String deviceLabel;
    private LocalDateTime lastActivityAt;
    private LocalDateTime absoluteExpirationAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
    private Boolean active;
}
