package in.abdulmajid.moneylog.feedback.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.NEW;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "steps_to_reproduce", columnDefinition = "text")
    private String stepsToReproduce;

    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @Column(name = "has_screenshot", nullable = false)
    @Builder.Default
    private Boolean hasScreenshot = false;

    @Column(name = "screenshot_filename", length = 255)
    private String screenshotFilename;

    @Column(name = "screenshot_path", columnDefinition = "text")
    private String screenshotPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus notificationStatus = NotificationStatus.NOT_SENT;

    @Column(name = "notification_error", columnDefinition = "text")
    private String notificationError;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}