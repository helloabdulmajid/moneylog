package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.dto.response.AdminFeedbackResponse;
import in.abdulmajid.moneylog.common.ResourceNotFoundException;
import in.abdulmajid.moneylog.feedback.model.Feedback;
import in.abdulmajid.moneylog.feedback.model.FeedbackCategory;
import in.abdulmajid.moneylog.feedback.model.FeedbackStatus;
import in.abdulmajid.moneylog.feedback.model.NotificationStatus;
import in.abdulmajid.moneylog.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("createdAt", "status", "category", "subject", "resolvedAt");

    private final FeedbackRepository feedbackRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<AdminFeedbackResponse> list(String status,
                                            String category,
                                            String notificationStatus,
                                            String search,
                                            int page,
                                            int size,
                                            String sortBy,
                                            String sortOrder) {
        AdminFeedbackSpecification spec = AdminFeedbackSpecification.builder()
                .status(parseEnum(status, FeedbackStatus.class))
                .category(parseEnum(category, FeedbackCategory.class))
                .notificationStatus(parseEnum(notificationStatus, NotificationStatus.class))
                .search(search)
                .build();

        String field = sortBy != null && SORTABLE_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return feedbackRepository
                .findAll(spec, PageRequest.of(page, Math.min(size, 100), Sort.by(direction, field)))
                .map(AdminFeedbackService::toDto);
    }

    @Transactional(readOnly = true)
    public AdminFeedbackResponse get(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public AdminFeedbackResponse updateStatus(UUID id, String status) {
        Feedback feedback = findOrThrow(id);

        FeedbackStatus newStatus = parseEnum(status, FeedbackStatus.class);
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }

        FeedbackStatus oldStatus = feedback.getStatus();
        feedback.setStatus(newStatus);
        feedback.setResolvedAt(newStatus == FeedbackStatus.CLOSED ? LocalDateTime.now() : null);
        feedbackRepository.save(feedback);

        auditService.record("feedback.status_changed", "feedback", id.toString(),
                oldStatus.name() + " -> " + newStatus.name());

        return toDto(feedback);
    }

    static AdminFeedbackResponse toDto(Feedback feedback) {
        return AdminFeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUser() != null ? feedback.getUser().getId() : null)
                .userEmail(feedback.getUser() != null ? feedback.getUser().getEmail() : null)
                .userName(feedback.getUser() != null ? feedback.getUser().getName() : null)
                .category(feedback.getCategory().name())
                .status(feedback.getStatus().name())
                .subject(feedback.getSubject())
                .description(feedback.getDescription())
                .stepsToReproduce(feedback.getStepsToReproduce())
                .contactEmail(feedback.getContactEmail())
                .hasScreenshot(feedback.getHasScreenshot())
                .notificationStatus(feedback.getNotificationStatus().name())
                .notificationError(feedback.getNotificationError())
                .submittedAt(feedback.getCreatedAt())
                .resolvedAt(feedback.getResolvedAt())
                .build();
    }

    private Feedback findOrThrow(UUID id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
    }

    private static <E extends Enum<E>> E parseEnum(String value, Class<E> type) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
    }
}
