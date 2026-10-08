package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {

    private UUID id;
    private String email;
    private String name;
    private String gender;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private Long activeSessions;
    private AdminUserPreferenceResponse preference;
    private List<AdminUserSessionResponse> sessions;
    private List<AdminFeedbackResponse> feedback;
}
