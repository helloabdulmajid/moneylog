package in.abdulmajid.moneylog.feedback.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.service.MailDeliveryResult;
import in.abdulmajid.moneylog.auth.service.MailService;
import in.abdulmajid.moneylog.feedback.dto.request.FeedbackRequest;
import in.abdulmajid.moneylog.feedback.dto.response.FeedbackResponse;
import in.abdulmajid.moneylog.feedback.model.Feedback;
import in.abdulmajid.moneylog.feedback.model.FeedbackStatus;
import in.abdulmajid.moneylog.feedback.model.NotificationStatus;
import in.abdulmajid.moneylog.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private static final long MAX_SCREENSHOT_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp");

    private final FeedbackRepository feedbackRepository;
    private final FeedbackRateLimiter rateLimiter;
    private final MailService mailService;

    @Transactional
    public FeedbackResponse submit(FeedbackRequest request, MultipartFile screenshot,
                                  User user, String clientIp) {
        rateLimiter.check(rateLimitKey(user, clientIp));

        byte[] screenshotBytes = null;
        String screenshotFilename = null;
        String screenshotMimeType = null;

        if (screenshot != null && !screenshot.isEmpty()) {
            screenshotBytes = readScreenshot(screenshot);
            screenshotMimeType = detectImageType(screenshotBytes);
            if (screenshotMimeType == null) {
                throw new RuntimeException("Screenshot must be a PNG, JPEG or WebP image");
            }
            screenshotFilename = sanitizeFilename(screenshot.getOriginalFilename());
        }

        Feedback feedback = Feedback.builder()
                .user(user)
                .category(request.getCategory())
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .stepsToReproduce(optionalTrim(request.getStepsToReproduce()))
                .contactEmail(optionalEmail(request.getContactEmail()))
                .hasScreenshot(screenshotBytes != null)
                .screenshotFilename(screenshotFilename)
                .status(FeedbackStatus.NEW)
                .notificationStatus(NotificationStatus.NOT_SENT)
                .build();

        feedbackRepository.saveAndFlush(feedback);

        try {
            MailDeliveryResult result = mailService.sendFeedbackNotification(
                    shortId(feedback.getId()),
                    feedback.getCategory().name(),
                    feedback.getSubject(),
                    feedback.getDescription(),
                    feedback.getStepsToReproduce(),
                    feedback.getContactEmail(),
                    LocalDateTime.now(ZoneOffset.UTC),
                    Boolean.TRUE.equals(feedback.getHasScreenshot()),
                    feedback.getScreenshotFilename(),
                    screenshotBytes,
                    screenshotMimeType);
            switch (result.status()) {
                case SENT -> {
                    feedback.setNotificationStatus(NotificationStatus.SENT);
                    feedback.setNotificationError(null);
                }
                case LOGGED -> {
                    feedback.setNotificationStatus(NotificationStatus.LOGGED);
                    feedback.setNotificationError(result.error());
                }
                case FAILED -> {
                    feedback.setNotificationStatus(NotificationStatus.FAILED);
                    feedback.setNotificationError(result.error());
                }
            }
        } catch (Exception e) {
            log.warn("Feedback {} saved but notification email failed: {}",
                    feedback.getId(), e.getMessage());
            feedback.setNotificationStatus(NotificationStatus.FAILED);
            feedback.setNotificationError("Failed to send feedback notification email");
        }

        return toResponse(feedback);
    }

    private byte[] readScreenshot(MultipartFile file) {
        if (file.getSize() <= 0) {
            throw new RuntimeException("Screenshot file is empty");
        }
        if (file.getSize() > MAX_SCREENSHOT_BYTES) {
            throw new RuntimeException("Screenshot must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("Screenshot must be a PNG, JPEG or WebP image");
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Could not read the screenshot", e);
        }
    }

    private String detectImageType(byte[] b) {
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "screenshot.png";
        }
        String name = Paths.get(original).getFileName().toString();
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.length() > 120) {
            name = name.substring(name.length() - 120);
        }
        return name;
    }

    private String optionalTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String optionalEmail(String value) {
        String trimmed = optionalTrim(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String rateLimitKey(User user, String clientIp) {
        if (user != null) {
            return "user:" + user.getId();
        }
        return "ip:" + ((clientIp == null || clientIp.isBlank()) ? "unknown" : clientIp);
    }

    private String shortId(UUID id) {
        return id.toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .category(feedback.getCategory().name())
                .status(feedback.getStatus().name())
                .subject(feedback.getSubject())
                .notificationStatus(feedback.getNotificationStatus().name())
                .submittedAt(feedback.getCreatedAt())
                .message(messageFor(feedback.getNotificationStatus()))
                .build();
    }

    private String messageFor(NotificationStatus status) {
        if (status == null) {
            return "Thanks! Your report was saved for review.";
        }
        switch (status) {
            case SENT:
                return "Thanks! Your report was received and the team has been notified.";
            case LOGGED:
                return "Thanks! Your report was saved, but the notification email wasn't delivered in this preview environment — it was written to the server log instead. The team will still review it.";
            case FAILED:
                return "Thanks! Your report was saved, but we couldn't send a notification email right now. Our team will still review it.";
            default:
                return "Thanks! Your report was saved for review.";
        }
    }
}