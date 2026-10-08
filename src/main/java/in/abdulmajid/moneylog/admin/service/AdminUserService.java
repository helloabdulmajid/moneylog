package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.dto.response.AdminFeedbackResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminUserDetailResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminUserPreferenceResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminUserResponse;
import in.abdulmajid.moneylog.admin.dto.response.AdminUserSessionResponse;
import in.abdulmajid.moneylog.auth.model.Session;
import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.SessionRepository;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.common.ResourceNotFoundException;
import in.abdulmajid.moneylog.feedback.repository.FeedbackRepository;
import in.abdulmajid.moneylog.user.model.UserPreference;
import in.abdulmajid.moneylog.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read-mostly user management for admins. Deliberately exposes NO financial
 * data: profile, preferences, sessions and feedback only.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final FeedbackRepository feedbackRepository;
    private final AuditService auditService;

    @Value("${session.inactivity-timeout}")
    private long sessionInactivityTimeoutMs;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> list(String search, Boolean emailVerified, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern)));
            }
            if (emailVerified != null) {
                predicates.add(cb.equal(root.get("emailVerified"), emailVerified));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<User> users = userRepository.findAll(spec,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));

        List<UUID> ids = users.getContent().stream().map(User::getId).toList();
        Map<UUID, Long> activeSessions = activeSessionCounts(ids);
        Map<UUID, Long> feedbackCounts = feedbackRepository.countByUserIdsAsMap(ids);

        return users.map(user -> toResponse(
                user,
                activeSessions.getOrDefault(user.getId(), 0L),
                feedbackCounts.getOrDefault(user.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse get(UUID id) {
        User user = findOrThrow(id);

        UserPreference preference = preferenceRepository.findByUserId(id).orElse(null);
        LocalDateTime activeThreshold = LocalDateTime.now().minus(sessionInactivityTimeoutMs, java.time.temporal.ChronoUnit.MILLIS);
        long activeSessions = sessionRepository
                .countActiveByUserIds(List.of(id), activeThreshold)
                .stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<AdminUserSessionResponse> sessions = sessionRepository
                .findByUserIdOrderByLastActivityAtDesc(id)
                .stream()
                .map(session -> toSessionResponse(session, activeThreshold))
                .toList();

        List<AdminFeedbackResponse> feedback = feedbackRepository
                .findByUserIdOrderByCreatedAtDesc(id, PageRequest.of(0, 20))
                .map(AdminFeedbackService::toDto)
                .getContent();

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .activeSessions(activeSessions)
                .preference(toPreferenceResponse(preference))
                .sessions(sessions)
                .feedback(feedback)
                .build();
    }

    @Transactional
    public Map<String, String> revokeSessions(UUID id) {
        User user = findOrThrow(id);
        int revoked = sessionRepository.revokeByUserId(id, LocalDateTime.now());
        auditService.record("user.sessions_revoked", "user", id.toString(),
                revoked + " session(s) revoked");
        return Map.of("message", revoked + " active session(s) revoked.");
    }

    @Transactional
    public Map<String, String> verifyEmail(UUID id) {
        User user = findOrThrow(id);
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return Map.of("message", "Email is already verified.");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        auditService.record("user.email_verified", "user", id.toString(), null);
        return Map.of("message", "Email verified.");
    }

    private Map<UUID, Long> activeSessionCounts(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        LocalDateTime threshold = LocalDateTime.now().minus(sessionInactivityTimeoutMs, java.time.temporal.ChronoUnit.MILLIS);
        return sessionRepository.countActiveByUserIds(ids, threshold).stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    private AdminUserResponse toResponse(User user, long activeSessions, long feedbackCount) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .activeSessions(activeSessions)
                .feedbackCount(feedbackCount)
                .build();
    }

    private AdminUserSessionResponse toSessionResponse(Session session, LocalDateTime activeThreshold) {
        boolean active = session.getRevokedAt() == null
                && session.getLastActivityAt().isAfter(activeThreshold);
        return AdminUserSessionResponse.builder()
                .deviceLabel(session.getDeviceLabel())
                .lastActivityAt(session.getLastActivityAt())
                .absoluteExpirationAt(session.getAbsoluteExpirationAt())
                .revokedAt(session.getRevokedAt())
                .createdAt(session.getCreatedAt())
                .active(active)
                .build();
    }

    private AdminUserPreferenceResponse toPreferenceResponse(UserPreference preference) {
        if (preference == null) {
            return null;
        }
        return AdminUserPreferenceResponse.builder()
                .theme(preference.getTheme() != null ? preference.getTheme().name() : null)
                .currency(preference.getCurrency())
                .timezone(preference.getTimezone())
                .dateFormat(preference.getDateFormat() != null ? preference.getDateFormat().name() : null)
                .timeFormat(preference.getTimeFormat() != null ? preference.getTimeFormat().name() : null)
                .language(preference.getLanguage())
                .billReminderEnabled(preference.getBillReminderEnabled())
                .mismatchAlertEnabled(preference.getMismatchAlertEnabled())
                .spendingSummaryEnabled(preference.getSpendingSummaryEnabled())
                .reminderDaysBefore(preference.getReminderDaysBefore() != null
                        ? preference.getReminderDaysBefore().name() : null)
                .build();
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
