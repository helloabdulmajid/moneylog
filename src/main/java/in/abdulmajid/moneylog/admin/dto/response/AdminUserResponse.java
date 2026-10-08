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
public class AdminUserResponse {

    private UUID id;
    private String email;
    private String name;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private Long activeSessions;
    private Long feedbackCount;
}
