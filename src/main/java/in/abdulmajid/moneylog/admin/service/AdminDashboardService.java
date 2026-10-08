package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.dto.response.AdminAuditLogResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminDashboardResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminFeedbackResponse;
import in.abdulmajid.moneylog.admin.repository.AuditLogRepository;
import in.abdulmajid.moneylog.auth.repository.SessionRepository;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.feedback.repository.FeedbackRepository;
import in.abdulmajid.moneylog.featureflag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${session.inactivity-timeout}")
    private long sessionInactivityTimeoutMs;

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime activeThreshold = now.minus(sessionInactivityTimeoutMs, java.time.temporal.ChronoUnit.MILLIS);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        feedbackRepository.countGroupByStatus()
                .forEach(row -> byStatus.put(String.valueOf(row[0]), (Long) row[1]));

        Map<String, Long> byCategory = new LinkedHashMap<>();
        feedbackRepository.countGroupByCategory()
                .forEach(row -> byCategory.put(String.valueOf(row[0]), (Long) row[1]));

        List<AdminFeedbackResponse> recentFeedback = feedbackRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(AdminFeedbackService::toDto)
                .toList();

        List<AdminAuditLogResponse> recentAudit = auditLogRepository
                .findRecentWithAdmin(PageRequest.of(0, 5))
                .stream()
                .map(AdminAuditService::toDto)
                .toList();

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .newUsers30d(userRepository.countByCreatedAtAfter(now.minusDays(30)))
                .activeUserSessions(sessionRepository.countByRevokedAtIsNullAndLastActivityAtAfter(activeThreshold))
                .feedbackTotal(feedbackRepository.count())
                .feedbackByStatus(byStatus)
                .feedbackByCategory(byCategory)
                .recentFeedback(recentFeedback)
                .flagsTotal(featureFlagRepository.count())
                .flagsEnabled(featureFlagRepository.countByEnabledTrue())
                .recentAudit(recentAudit)
                .build();
    }
}
