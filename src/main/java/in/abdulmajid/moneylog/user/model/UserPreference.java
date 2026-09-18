package in.abdulmajid.moneylog.user.model;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ThemePreference theme = ThemePreference.SYSTEM;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DateFormat dateFormat = DateFormat.DD_MMM_YYYY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TimeFormat timeFormat = TimeFormat.TWENTY_FOUR_HOUR;

    @Column(nullable = false)
    @Builder.Default
    private String language = "en";

    @Column(nullable = false)
    @Builder.Default
    private Boolean billReminderEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mismatchAlertEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean spendingSummaryEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReminderTiming reminderDaysBefore = ReminderTiming.DAYS_7;
}
