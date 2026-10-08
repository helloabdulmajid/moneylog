package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private Long totalUsers;
    private Long newUsers30d;
    private Long activeUserSessions;
    private Long feedbackTotal;
    private Map<String, Long> feedbackByStatus;
    private Map<String, Long> feedbackByCategory;
    private List<AdminFeedbackResponse> recentFeedback;
    private Long flagsTotal;
    private Long flagsEnabled;
    private List<AdminAuditLogResponse> recentAudit;
}
